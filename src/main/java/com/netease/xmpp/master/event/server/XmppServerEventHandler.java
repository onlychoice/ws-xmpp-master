package com.netease.xmpp.master.event.server;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;
import com.netease.xmpp.master.server.ClientCache;
import com.netease.xmpp.master.server.ClientNotifier;

public class XmppServerEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(XmppServerEventHandler.class);

    private ClientCache clientCache = null;

    public XmppServerEventHandler(ClientCache cache) {
        this.clientCache = cache;
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        Channel channel = ctx.getChannel();
        EventType type = ctx.getEvent();

        Throwable exception = null;
        if (type == EventType.SERVER_EXCEPTION) {
            exception = (Throwable) ctx.getData();
        }

        switch (type) {
        case SERVER_CONNECT:
            clientCache.addXmppServer(channel);
            ClientNotifier.notifyAllServerUpdate();

            ctx.getDispatcher().dispatchEvent(channel, null, EventType.SERVER_HEARTBEAT_START);
            break;
        case SERVER_DISCONNECT:
            ctx.getDispatcher().dispatchEvent(channel, null, EventType.SERVER_HEARTBEAT_STOP);

            clientCache.removeXmppServer(channel);
            ClientNotifier.notifyAllServerUpdate();
            break;
        case SERVER_HEARTBEAT_TIMEOUT:
            channel.close();
            break;
        case SERVER_EXCEPTION:
            logger.error(exception.toString());
            break;
        default:
            throw new UnrecognizedEvent(type.toString());
        }
    }
}
