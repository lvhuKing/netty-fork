package com.nettyfork.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * 手写一个NIO客户端
 * @author ccl
 * @date 2021/10/22 15:14
 */
public class NioClientDemo {

    private Selector selector ;

    private SocketChannel socketChannel ;

    public NioClientDemo() {
        try {
            for(int i=0; i<3; i++){
                selector = Selector.open();
                socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1"+i, 8082));
                socketChannel.configureBlocking(false) ;
                socketChannel.register(selector, SelectionKey.OP_READ);
                String username = socketChannel.getLocalAddress().toString().substring(1);
                System.out.println(username + " is  OK");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInfo(String info){
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readInfo(){
        try {
            int count = selector.select();
            if(count > 0){
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    if(key.isReadable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.read(buffer);
                        String msg = new String(buffer.array(), 0, buffer.limit());
                        System.out.println(msg.trim());
                    }

                    iterator.remove();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NioClientDemo client = new NioClientDemo();
        //启动一个线程，进行数据的读取
        new Thread(){
            @Override
            public void run() {
                while (true){
                    client.readInfo();
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //发送数据
        Scanner scanner = new Scanner(System.in) ;
        while (scanner.hasNextLine()){
            String msg = scanner.nextLine();
            client.sendInfo(msg);
        }
    }
}


