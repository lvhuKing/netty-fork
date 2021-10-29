package com.nettyfork.socket.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 参考： https://blog.csdn.net/qmqm011/article/details/100156010/
 * 服务端启动类
 * @author ccl
 * @date 2021/10/26 10:16
 */
@Slf4j
@Component
public class Server {

    @Value("${socket.simple.port}")
    private Integer port;
    @Resource
    private ServerChannelInitializer serverChannelInitializer;

    /**
     * bossGroup: boss“老板”事件轮询线程组，用来接收连接，只需一个线程负责即可
     * workerGroup: worker“工人”事件轮询线程组，处理完成三次握手的连接套接字的IO读写请求，线程数默认为CPU核心数的2倍
     */
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;

    public void start(){
        ChannelFuture channelFuture = null;
        try {
            // 服务端启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workerGroup)
                    // 设置通道类型
                    .channel(NioServerSocketChannel.class)
                    // 设置通道数据处理对象
                    .childHandler(serverChannelInitializer)
                    // 设置通道连接的TCP参数：请求队列最大值
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 使消息立即发出，不用等到一定的数据量再发出去
                    .option(ChannelOption.TCP_NODELAY, true)
                    // 保持长连接状态，心跳保活（默认2小时发一次心跳）
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口、开启监听、同步等待
            channelFuture = serverBootstrap.bind(port).sync();
            if(channelFuture.isSuccess()){
                log.info("Netty启动成功，监听端口：" + port);
            }else{
                log.info("Netty启动失败，监听端口：" + port);
            }
            // 等待服务端监听端口关闭
//            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Netty启动异常，监听端口：" + port);
        } finally {
            // 退出，关闭线程资源
//            workerGroup.shutdownGracefully();
//            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 停止Netty服务端
     */
    public void destroy(){
        if(channel != null){
            channel.close();
        }
        try {
            Future<?> future = workerGroup.shutdownGracefully().await();
            if(!future.isSuccess()){
                log.error("Netty tcp workerGroup关闭失败：" + future.cause());
            }else{
                log.info("Netty tcp workerGroup关闭成功");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Netty tcp workerGroup关闭失败：" + e.getMessage());
        }
    }

}
