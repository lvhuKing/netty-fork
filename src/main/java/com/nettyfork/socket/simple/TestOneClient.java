package com.nettyfork.socket.simple;

import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 测试Socket发送单条消息
 * @author ccl
 * @date 2021/10/26 14:33
 */
public class TestOneClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 10001);
            String msg = "今天是个好天气\n";
            OutputStream out = socket.getOutputStream();
            out.write(msg.getBytes(CharsetUtil.UTF_8));
            out.flush();
            Thread.sleep(1000);
            // 读取数据，存到缓存区
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            char[] ch = new char[65536];
            int len;
            while((len = br.read(ch)) != -1){
                String result = new String(ch, 0, len);
                System.out.println("返回输出数据:" + result);
            }
            br.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
