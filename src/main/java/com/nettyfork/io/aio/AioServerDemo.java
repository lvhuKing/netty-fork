package com.nettyfork.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 手写一个AIO服务器
 * @author ccl
 * @date 2021/10/22 10:16
 */
public class AioServerDemo {

    public void startListen(int port) throws InterruptedException {
        try {
            AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.accept(null,new CompletionHandler<AsynchronousSocketChannel,Void>() {
                @Override
                public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                    serverSocketChannel.accept(null,this); //收到连接后，应该调用accept方法等待新的连接进来
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    socketChannel.read(byteBuffer,byteBuffer, new CompletionHandler<Integer,ByteBuffer>() {
                        @Override
                        public void completed(Integer num, ByteBuffer attachment) {
                            if (num > 0){
                                attachment.flip();
                                System.out.println(new String(attachment.array()).trim());
                            }else {
                                try {
                                    socketChannel.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            System.out.println("read error");
                            exc.printStackTrace();
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.out.println("accept error");
                    exc.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        //模拟去做其他事情
        while (true){
            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AioServerDemo aioServer = new AioServerDemo();
        aioServer.startListen(8080);
    }
}

