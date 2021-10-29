package com.nettyfork.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * 手写一个BIO服务器（同步阻塞）
 * 为每一个请求都创建一个线程，如果请求的并发量过大，则有可能服务器资源耗尽
 * 如果使用线程池，则如果客户端建立连接之后不发送数据，则有可能线程池中的线程全部阻塞，造成服务器瘫痪
 * 解决办法：为每个socket限制时间，一旦超时则关闭socket
 * 更好的解决办法是：只有客户端发生事件，才创建线程或者交给线程池处理，这样就可以减少线程数量，
 * 减少资源消耗和上下文切换的开销，这个时候可以使用NIO的方式
 * @author ccl
 * @date 2021/10/22 10:10
 */
public class BioServerDemo {

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8081);
            while (true){
                System.out.println("等待客户端连接...");
                Socket socket = serverSocket.accept();
                // 设置超时时间，一旦超时则关闭socket
                socket.setSoTimeout(50000);
                ReadThread readThread = new ReadThread(socket);
                readThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ReadThread extends Thread{
        private Socket socket;

        public ReadThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // 发送消息
                OutputStream out = socket.getOutputStream();
                out.write(("BIO服务器收到你的连接！").getBytes());
                out.flush();
                // 接收消息
                System.out.println("客户端已连接，接收消息：");
                try {
                    InputStream input = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    // 同步阻塞 读取客户端数据 直到输入数据
                    while (input.read(bytes) != -1) {
                        out.write(("收到了你的消息:" + new String(bytes)).getBytes());
                        out.flush();
                        System.out.println("收到客户端消息:" + new String(bytes));
                    }
                } catch (IOException ex) {
                    // 客户端链接断开则引发异常
                    System.err.println("引发异常：" + ex.getMessage());
                }
            } catch (SocketTimeoutException e) {
                System.out.println("socket超时");
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

