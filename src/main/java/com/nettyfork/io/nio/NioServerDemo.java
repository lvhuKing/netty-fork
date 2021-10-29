package com.nettyfork.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 手写一个NIO服务器（同步非阻塞）
 * 一个线程接收请求，一个线程执行IO操作
 * @author ccl
 * @date 2021/10/22 10:13
 */
public class NioServerDemo {

    private Selector selector ;

    private ServerSocketChannel serverChannel ;


    private Map<SocketChannel, String> usernameMap = new HashMap<>();

    public NioServerDemo() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false) ;
            serverChannel.bind(new InetSocketAddress(8082));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        while (true){
            int count = 0;
            try {
                count = selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(count > 0){
                // selector.selectedKeys() 返回注册在selector中等待IO操作(及有事件发生)channel的selectionKey
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isAcceptable()){
                        try {
                            // 客户端socketChannel
                            SocketChannel socketChannel = serverChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                            System.out.println(socketChannel.getRemoteAddress() + " 上线了");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(selectionKey.isReadable()){
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        try {
                            ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                            buffer.clear();
                            int readCount = socketChannel.read(buffer);
                            buffer.flip();
                            if(readCount > 0){
                                String message = new String(buffer.array(), 0, buffer.limit());
                                // 收到消息后，做出回应
                                // selector.keys 返回当前所有注册在selector中channel的selectionKey
                                Set<SelectionKey> allKeys = selector.keys();
                                for (SelectionKey key : allKeys) {
                                    Channel channel = key.channel();
                                    if(channel instanceof SocketChannel && channel == socketChannel){
                                        String username = usernameMap.get(socketChannel);
                                        if(null == username || username.equals("")){
                                            username = ((SocketChannel) channel).getRemoteAddress().toString().substring(1);
                                        }
                                        String msg = username + " 说: " + message ;
                                        ByteBuffer returnBuffer = ByteBuffer.wrap(msg.getBytes());
                                        try {
                                            ((SocketChannel) channel).write(returnBuffer);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            try {
                                System.out.println(socketChannel.getRemoteAddress() + " 下线了");
                                selectionKey.cancel();
                                socketChannel.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    iterator.remove();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NioServerDemo server = new NioServerDemo();
        server.listen();
    }
}



