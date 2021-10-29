package com.nettyfork.socket.simple;

import io.netty.channel.ChannelInitializer;
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
 * 客户端
 * @author ccl
 * @date 2021/10/26 13:56
 */
@Slf4j
@Component
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    private ClientChannelHandler clientChannelHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new IdleStateHandler(10, 0, 0))
                .addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE))
                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                .addLast(new StringEncoder(CharsetUtil.UTF_8))
                .addLast(clientChannelHandler);
    }
}
