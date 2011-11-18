package com.netease.xmpp.master.event.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.netease.xmpp.master.common.ConfigCache;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.common.MessageFlag;
import com.netease.xmpp.master.common.ServerListProtos.Server.ServerInfo;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;
import com.netease.xmpp.master.server.ClientCache;
import com.netease.xmpp.master.server.ClientNotifier;

public class XmppServerEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(XmppServerEventHandler.class);

    private final Message acceptMessage;

    private ClientCache clientCache = null;

    public XmppServerEventHandler(ClientCache cache, ConfigCache serverConfig) {
        this.clientCache = cache;
        byte[] data = serverConfig.getXmppDomain().getBytes();
        acceptMessage = new Message(MessageFlag.FLAG_SERVER_INFO_ACCEPTED, 0, data.length, data);
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        Channel channel = ctx.getChannel();
        EventType type = ctx.getEvent();

        Message data = null;
        Throwable exception = null;
        if (type == EventType.ROBOT_EXCEPTION) {
            exception = (Throwable) ctx.getData();
        } else {
            data = (Message) ctx.getData();
        }

        switch (type) {
        case SERVER_CONNECT:
            // Do nothing
            break;
        case SERVER_INFO_RECV:
            ServerInfo.Builder serverInfoBuilder = ServerInfo.newBuilder();
            serverInfoBuilder = serverInfoBuilder.mergeFrom(data.getData());
            InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();

            if (!checkCacheHost(serverInfoBuilder.getCacheHost())) {
                serverInfoBuilder.setCacheHost(address.getAddress().getHostAddress());
            }
            serverInfoBuilder.setIp(address.getAddress().getHostAddress());

            channel.write(acceptMessage);

            ServerInfo addServer = serverInfoBuilder.build();
            clientCache.addXmppServer(channel, addServer);

            ClientNotifier.notifyAllServerAdded(addServer);

            ctx.getDispatcher().dispatchEvent(channel, null, EventType.SERVER_HEARTBEAT_START);
            break;
        case SERVER_DISCONNECT:
            ctx.getDispatcher().dispatchEvent(channel, null, EventType.SERVER_HEARTBEAT_STOP);

            ServerInfo delServer = clientCache.getXmppServer(channel);
            clientCache.removeXmppServer(channel);

            ClientNotifier.notifyAllServerDown(delServer);
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

    private boolean checkCacheHost(String host) {
        if (host.equals("localhost") || host.equals("127.0.0.1")) {
            return false;
        }

        return true;
    }
}
