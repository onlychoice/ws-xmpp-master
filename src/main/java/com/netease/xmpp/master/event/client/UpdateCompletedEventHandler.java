package com.netease.xmpp.master.event.client;

import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.netease.xmpp.master.common.MessageFlag;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;

public class UpdateCompletedEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(UpdateCompletedEventHandler.class);

    private Random random = new Random(System.currentTimeMillis());

    @Override
    public void handle(EventContext ctx) throws IOException {
        EventType event = ctx.getEvent();
        Channel serverChannel = ctx.getChannel();
        Message data = (Message) ctx.getData();

        switch (event) {
        case CLIENT_SERVER_UPDATE_COMPLETE:
            sleep(); // TODO test code
            serverChannel.write(new Message(MessageFlag.FLAG_SERVER_UPDATE_COMPLETE, data
                    .getVersion(), 0, null));
            break;
        case CLIENT_HASH_UPDATE_COMPLETE:
            sleep(); // TODO test code
            serverChannel.write(new Message(MessageFlag.FLAG_HASH_UPDATE_COMPLETE, data
                    .getVersion(), 0, null));
            break;
        default:
            throw new UnrecognizedEvent(event.toString());
        }
    }

    /**
     * For testing, give 1~3sec random latency to the client.
     */
    private void sleep() {
        int time = random.nextInt(3) + 1;

        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            // Do nothing
        }
    }
}
