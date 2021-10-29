package com.nettyfork.socket.simple;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ChannelHandler.Sharable 同一个Handle实例可以添加到多个ChannelPipeline中共享，但并意味着单例
 * @author ccl
 * @date 2021/10/26 11:14
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ServerChannelHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 获取Client对象 ip:port
     */
    public String getRemoteAddress(ChannelHandlerContext ctx){
        return ctx.channel().remoteAddress().toString();
    }

    /**
     * 收发数据、处理业务
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        Channel channel = ctx.channel();
        // 处理消息
        if(log.isInfoEnabled()){
            log.info("服务端收到消息：" + object);
        }
        // 服务端回应消息
        channel.writeAndFlush("已收到：" + object.toString() + "\n").sync();
    }

    /**
     * 第一次连接成功后执行该方法
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("tcp client "+getRemoteAddress(ctx)+" connect success");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    /**
     * 超时处理：心跳保活
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        String clientAddress = getRemoteAddress(ctx);
        if(IdleStateEvent.class.isAssignableFrom(evt.getClass())){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(IdleState.READER_IDLE == event.state()){
                log.warn(clientAddress + "READER_IDLE：读超时");
                ctx.disconnect();
            }else if(IdleState.WRITER_IDLE == event.state()){
                log.warn(clientAddress + "WRITER_IDLE：写超时");
                ctx.disconnect();
            }else if(IdleState.ALL_IDLE == event.state()){
                log.warn(clientAddress + "ALL_IDLE：总超时");
                ctx.disconnect();
            }
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("服务端异常" + cause.getMessage());
        ctx.channel().writeAndFlush("服务端异常：" + cause.getMessage() + "\n").sync();
        ctx.close();
    }
}
