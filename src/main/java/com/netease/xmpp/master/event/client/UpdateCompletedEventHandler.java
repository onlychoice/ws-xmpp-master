package com.netease.xmpp.master.event.client;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.netease.xmpp.master.client.ClientGlobal;
import com.netease.xmpp.master.common.MessageFlag;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;

public abstract class UpdateCompletedEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(UpdateCompletedEventHandler.class);

    @Override
    public void handle(EventContext ctx) throws IOException {
        EventType event = ctx.getEvent();
        Channel serverChannel = ctx.getChannel();
        Message data = (Message) ctx.getData();

        switch (event) {
        case CLIENT_SERVER_UPDATE_COMPLETE:
            logger.debug("Server info updated: " + data.getVersion());
            serverChannel.write(new Message(MessageFlag.FLAG_SERVER_UPDATE_COMPLETE, data
                    .getVersion(), 0, null));
            break;
        case CLIENT_HASH_UPDATE_COMPLETE:
            logger.debug("Hash info updated: " + data.getVersion());
            serverChannel.write(new Message(MessageFlag.FLAG_HASH_UPDATE_COMPLETE, data
                    .getVersion(), 0, null));
            break;
        case CLIENT_SERVER_ALL_COMPLETE:
            logger.debug("All server info updated: " + data.getVersion());
            ClientGlobal.setIsAllServerUpdated(true);
            allServerUpdated();
            break;
        case CLIENT_HASH_ALL_COMPLETE:
            logger.debug("All hash info updated: " + data.getVersion());
            ClientGlobal.setIsAllHashUpdated(true);
            allHashUpdated();
            break;
        default:
            throw new UnrecognizedEvent(event.toString());
        }
    }

    public abstract void allServerUpdated();

    public abstract void allHashUpdated();
}
