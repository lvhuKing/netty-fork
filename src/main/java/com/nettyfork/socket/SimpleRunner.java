package com.nettyfork.socket;

import com.nettyfork.socket.simple.Client;
import com.nettyfork.socket.simple.Server;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author ccl
 * @date 2021/10/26 17:08
 */
@Slf4j
@Component
public class SimpleRunner implements CommandLineRunner {

    @Resource
    private Server server;
    @Resource
    private Client client;

    @Override
    public void run(String... args) throws Exception {
        log.info("===================启动一个简单的Netty通信测试===============");
        // 启动服务端
        ChannelFuture channelFuture = server.start();
        if(channelFuture != null){
            // 客户端连接、并发送消息
            client.connect();
            int num = 100;
            for(int i = 0; i < num; i++){
                client.sendMsg("第" + i + "天是晴天！");
            }
            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        }
    }
}
