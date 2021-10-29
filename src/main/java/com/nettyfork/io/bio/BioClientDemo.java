package com.nettyfork.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 手写一个BIO客户端
 * @author ccl
 * @date 2021/10/22 15:20
 */
public class BioClientDemo {

    public static void main(String[] args) {
        try {
            // 接收控制台用户输入的数据
            Scanner scanner = new Scanner(System.in);
            // 要绑定的主机和端口号
            Socket socket = new Socket("127.0.0.1", 8081);

            // 启动线程监听服务端发送的消息
            new Thread(() -> {
                try {
                    InputStream input = socket.getInputStream();
                    byte[] bytes = new byte[1024 * 4];
                    while ((input.read(bytes)) != -1) {
                        System.out.println("收到服务端消息:" + new String(bytes));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                try {
                    OutputStream output = socket.getOutputStream();
                    while (true) {
                        String str = scanner.nextLine();
                        if ("exit".equals(str)) {
                            output.close();
                            socket.close();
                            break;
                        } else {
                            // 向服务端发送消息
                            output.write(str.getBytes());
                            output.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

