package com.netease.xmpp.master.event.server;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;
import com.netease.xmpp.master.server.ClientCache;
import com.netease.xmpp.master.server.ClientNotifier;

public class ProxyEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(ProxyEventHandler.class);

    private ClientCache clientCache = null;

    public ProxyEventHandler(ClientCache cache) {
        this.clientCache = cache;
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        Channel channel = ctx.getChannel();
        EventType type = ctx.getEvent();

        Message data = null;
        Throwable exception = null;
        if (type == EventType.PROXY_EXCEPTION) {
            exception = (Throwable) ctx.getData();
        } else {
            data = (Message) ctx.getData();
        }

        switch (type) {
        case PROXY_CONNECT:
            clientCache.addProxy(channel);
            ClientNotifier.notifyServerUpdate(channel);
            ClientNotifier.notifyHashUpdate(channel);

            ctx.getDispatcher().dispatchEvent(channel, null, EventType.PROXY_HEARTBEAT_START);
            break;
        case PROXY_DISCONNECT:
            ctx.getDispatcher().dispatchEvent(channel, null, EventType.PROXY_HEARTBEAT_STOP);
            clientCache.removeProxy(channel);
            break;
        case PROXY_HEARTBEAT_TIMEOUT:
            channel.close();
            break;
        case PROXY_EXCEPTION:
            logger.error(exception.toString());
            // TODO proxy exception
            break;
        case PROXY_SERVER_UPDATE_COMPLETE:
            if (data.getVersion() == clientCache.getServerVersion()) {
                clientCache.setProxyServerSync(channel, true);
                ctx.getDispatcher().dispatchEvent(channel, ctx.getData(), EventType.SERVER_SYNCED);
            }
            break;
        case PROXY_HASH_UPDATE_COMPLETE:
            if (data.getVersion() == clientCache.getHashVersion()) {
                clientCache.setProxyHashSync(channel, true);
                ctx.getDispatcher().dispatchEvent(channel, ctx.getData(), EventType.HASH_SYNCED);
            }
            break;
        default:
            throw new UnrecognizedEvent(type.toString());
        }
    }
}
