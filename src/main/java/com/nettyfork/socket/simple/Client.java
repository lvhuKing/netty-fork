package com.nettyfork.socket.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author ccl
 * @date 2021/10/26 13:41
 */
@Slf4j
@Component
public class Client {

    private Channel channel;
    @Value("${socket.simple.host}")
    private String host;
    @Value("${socket.simple.port}")
    private Integer port;

    @Resource
    private ClientChannelInitializer clientChannelInitializer;

    /**
     * 初始化客户端启动类Bootstrap
     */
    private Bootstrap getBootstrap(){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                // 通道连接者
                .channel(NioSocketChannel.class)
                // 通道处理者
                .handler(clientChannelInitializer)
                // 心跳保活
                .option(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap;
    }

    /**
     * 与服务端建立连接
     */
    public void connect(){
        try {
            ChannelFuture channelFuture = getBootstrap().connect(host, port).sync();
            if(channelFuture != null && channelFuture.isSuccess()){
                channel = channelFuture.channel();
                log.info("连接服务器：" + host + ":" + port + "成功");
            }else{
                log.info("连接服务器：" + host + ":" + port + "失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("连接服务器：" + host + ":" + port + "异常：" + e.getMessage());
        }
    }

    /**
     * 向服务端发送消息
     */
    public void sendMsg(String msg){
        try {
            if(channel == null){
                log.warn("连接未建立，客户端无法发送消息");
            }else{
                channel.writeAndFlush(msg + "\n").sync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("连接未建立，客户端发送消息失败：" + e.getMessage());
        }
    }



}
