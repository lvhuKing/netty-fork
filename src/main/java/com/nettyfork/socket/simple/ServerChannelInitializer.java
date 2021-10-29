package com.nettyfork.socket.simple;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ChannelInitializer 帮助我们配置Channel
 * @author ccl
 * @date 2021/10/26 10:27
 */
@Slf4j
@Component
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    private ServerChannelHandler serverChannelHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        if(log.isInfoEnabled()){
            log.info("收到来自" + socketChannel.remoteAddress().getAddress() + "的连接");
        }
        ChannelPipeline pipeline = socketChannel.pipeline();
        // IdleStateHandler心跳机制，如果超时触发Handler中userEventTriggered方法
        pipeline.addLast(new IdleStateHandler(10, 0, 0))
                // 识别接收消息中的“\n”或“\r\n”，保证消息的完整性，避免TCP粘包拆包问题
                .addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE))
                // 字符串解码编码器
                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                .addLast(new StringEncoder(CharsetUtil.UTF_8))
                // 自定义Handler：收发数据、处理业务
                .addLast(serverChannelHandler);
    }

}
