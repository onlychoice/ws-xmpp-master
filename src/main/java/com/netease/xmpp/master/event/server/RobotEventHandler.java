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

public class RobotEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(RobotEventHandler.class);

    private ClientCache clientCache = null;

    public RobotEventHandler(ClientCache cache) {
        this.clientCache = cache;
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
        case ROBOT_CONNECT:
            clientCache.addRobot(channel);
            ClientNotifier.notifyServerUpdate(channel);
            ClientNotifier.notifyHashUpdate(channel);

            ctx.getDispatcher().dispatchEvent(channel, null, EventType.ROBOT_HEARTBEAT_START);
            break;
        case ROBOT_DISCONNECT:
            ctx.getDispatcher().dispatchEvent(channel, null, EventType.ROBOT_HEARTBEAT_STOP);
            clientCache.removeRobot(channel);
            break;
        case ROBOT_HEARTBEAT_TIMEOUT:
            channel.close();
            break;
        case ROBOT_EXCEPTION:
            logger.error(exception.toString());
            // TODO robot exception
            break;
        case ROBOT_SERVER_UPDATE_COMPLETE:
            if (data.getVersion() == clientCache.getServerVersion()) {
                clientCache.setRobotServerSync(channel, true);
                ctx.getDispatcher().dispatchEvent(channel, ctx.getData(), EventType.SERVER_SYNCED);
            }
            break;
        case ROBOT_HASH_UPDATE_COMPLETE:
            if (data.getVersion() == clientCache.getHashVersion()) {
                clientCache.setRobotHashSync(channel, true);
                ctx.getDispatcher().dispatchEvent(channel, ctx.getData(), EventType.HASH_SYNCED);
            }
            break;
        default:
            throw new UnrecognizedEvent(type.toString());
        }
    }
}
