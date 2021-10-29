package com.nettyfork.socket.simple;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author ccl
 * @date 2021/10/26 14:00
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ClientChannelHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        log.info("客户端收到消息：" + msg.toString());
    }

}
