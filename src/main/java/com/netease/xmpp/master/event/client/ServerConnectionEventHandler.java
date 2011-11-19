package com.netease.xmpp.master.event.client;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.netease.xmpp.master.common.ConfigConst;
import com.netease.xmpp.master.common.HeartBeatWorker;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventDispatcher;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;

public class ServerConnectionEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(ServerConnectionEventHandler.class);

    private ClientBootstrap bootstrap = null;
    private HeartBeatWorker worker = null;

    protected volatile long timeoutTime = -1;

    protected Channel serverChannel = null;

    private Thread timeoutChecker = null;

    private EventDispatcher eventDispatcher = null;

    public ServerConnectionEventHandler(ClientBootstrap bootstrap, EventDispatcher dispatcher) {
        this.bootstrap = bootstrap;
        this.eventDispatcher = dispatcher;

        timeoutChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // Read time
                    long time = timeoutTime;
                    if (time > 0 && time <= System.currentTimeMillis()) {
                        logger.debug("SERVER_HEARTBEAT_TIMOUT");
                        eventDispatcher.dispatchEvent(null, null,
                                EventType.CLIENT_SERVER_HEARTBEAT_TIMOUT);
                    }

                    try {
                        Thread.sleep(ConfigConst.HEARTBEAT_INTERVAL * 1000);
                    } catch (InterruptedException e) {
                        // Do nothing
                    }
                }
            }
        });
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        EventType event = ctx.getEvent();
        Channel channel = ctx.getChannel();

        long timeoutValue = System.currentTimeMillis() + ConfigConst.HEARTBEAT_TIMEOUT * 1000;
        switch (event) {
        case CLIENT_SERVER_CONNECTED:
            serverChannel = channel;
            startHeartBeat();
            timeoutTime = timeoutValue;
            break;

        case CLIENT_SERVER_HEARTBEAT_TIMOUT:
            timeoutTime = -1;
            serverChannel.close().awaitUninterruptibly();
            break;

        case CLIENT_SERVER_DISCONNECTED:
            reconnect();
            break;

        case CLIENT_SERVER_HEARTBEAT:
            timeoutTime = timeoutValue;
            break;
        default:
            throw new UnrecognizedEvent(event.toString());
        }
    }

    protected void startHeartBeat() {
        worker = new HeartBeatWorker(serverChannel);
        worker.start();
        timeoutChecker.start();
    }

    private void reconnect() {
        if (worker != null) {
            worker.stop();
        }

        while (true) {
            logger.info("START RECONNECTING......");

            ChannelFuture f = bootstrap.connect();
            f.awaitUninterruptibly();

            if (f.isDone() && f.isSuccess()) {
                logger.info("SERVER CONNECTED");
                break;
            }

            try {
                // sleep 5 sec for next retry
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // continue
            }
        }
    }
}
