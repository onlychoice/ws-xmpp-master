package com.netease.xmpp.master.server;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.netease.xmpp.master.common.MessageFlag;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.event.EventDispatcher;
import com.netease.xmpp.master.event.EventType;

/**
 * Client channel handler for master server.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public class CommonClientChannelHandler extends SimpleChannelHandler {
    private static Logger logger = Logger.getLogger(CommonClientChannelHandler.class);

    private EventDispatcher dispatcher = null;

    private String handlerPrefix = null;

    public CommonClientChannelHandler(EventDispatcher dispatcher, String handlerPrefix) {
        this.dispatcher = dispatcher;
        this.handlerPrefix = handlerPrefix;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Channel channel = e.getChannel();
        Message message = (Message) e.getMessage();
        byte flag = message.getFlag();

        if (flag == MessageFlag.FLAG_SERVER_UPDATE_COMPLETE) {
            logger.debug(handlerPrefix + " - SERVER_UPDATE_COMPLETE: " + channel.getRemoteAddress());

            dispatcher.dispatchEvent(channel, message, EventType.valueOf(handlerPrefix
                    + "_SERVER_UPDATE_COMPLETE"));
        } else if (flag == MessageFlag.FLAG_HASH_UPDATE_COMPLETE) {
            logger.debug(handlerPrefix + " - HASH_UPDATE_COMPLETE: " + channel.getRemoteAddress());

            dispatcher.dispatchEvent(channel, message, EventType.valueOf(handlerPrefix
                    + "_HASH_UPDATE_COMPLETE"));
        } else if (flag == MessageFlag.FLAG_SERVER_INFO) {
            // Only XMPP server send this type of message to the master
            logger.debug(handlerPrefix + " - INFO_RECV: " + channel.getRemoteAddress());

            dispatcher.dispatchEvent(channel, message, EventType.SERVER_INFO_RECV);
        }else if (flag == MessageFlag.FLAG_HEATBEAT) {
            logger.debug(handlerPrefix + " - HEARTBEAT: " + channel.getRemoteAddress());

            dispatcher.dispatchEvent(channel, message, EventType.valueOf(handlerPrefix
                    + "_HEARTBEAT"));
        }
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel channel = e.getChannel();
        logger.debug(handlerPrefix + " - CONNECTED: " + channel.getRemoteAddress());

        dispatcher.dispatchEvent(channel, null, EventType.valueOf(handlerPrefix + "_CONNECT"));
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel channel = e.getChannel();
        logger.debug(handlerPrefix + " - DISCONNECTED: " + channel.getRemoteAddress());

        dispatcher.dispatchEvent(channel, null, EventType.valueOf(handlerPrefix + "_DISCONNECT"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Channel channel = e.getChannel();
        logger.debug(handlerPrefix + " - EXCEPTION: " + channel.getRemoteAddress());

        channel.close();
        
        dispatcher.dispatchEvent(channel, e.getCause(), EventType.valueOf(handlerPrefix
                + "_EXCEPTION"));
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) {
        Message message = (Message) e.getMessage();
        int bufSize = message.getMessageSize();
        ChannelBuffer buffer = ChannelBuffers.directBuffer(bufSize);
        buffer.writeByte(message.getFlag());
        buffer.writeInt(message.getVersion());
        buffer.writeInt(message.getDataLength());
        if (message.getDataLength() > 0) {
            buffer.writeBytes(message.getData());
        }

        Channels.write(ctx, e.getFuture(), buffer);
    }
}