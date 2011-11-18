package com.netease.xmpp.master.event.client;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.netease.xmpp.master.client.ClientGlobal;
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

    protected AtomicLong timeoutTime = new AtomicLong(-1);

    protected Channel serverChannel = null;

    private Thread timeoutChecker = null;

    private EventDispatcher eventDispatcher = null;

    private Semaphore sep = new Semaphore(1);
    private boolean isConnected = false;

    public ServerConnectionEventHandler(ClientBootstrap bootstrap, EventDispatcher dispatcher) {
        this.bootstrap = bootstrap;
        this.eventDispatcher = dispatcher;

        timeoutChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (timeoutTime) {
                        if (timeoutTime.get() > 0) {
                            if (timeoutTime.get() <= System.currentTimeMillis()) {
                                logger.debug("SERVER_HEARTBEAT_TIMOUT");
                                eventDispatcher.dispatchEvent(null, null,
                                        EventType.CLIENT_SERVER_HEARTBEAT_TIMOUT);
                            }
                        }
                    }

                    try {
                        Thread.sleep(ConfigConst.HEARTBEAT_INTERVAL * 1000);
                    } catch (InterruptedException e) {
                        // Do nothing
                    }
                }
            }
        });

        timeoutChecker.start();
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        EventType event = ctx.getEvent();
        Channel channel = ctx.getChannel();

        long timeoutValue = System.currentTimeMillis() + ConfigConst.HEARTBEAT_TIMEOUT * 1000;
        switch (event) {
        case CLIENT_SERVER_CONNECTED:
            ClientGlobal.setIsMasterAlive(true);

            serverChannel = channel;
            startHeartBeat();
            synchronizedSet(timeoutTime, timeoutValue);
            break;

        case CLIENT_SERVER_HEARTBEAT_TIMOUT:
            ClientGlobal.setIsMasterAlive(false);

            synchronizedSet(timeoutTime, -1);
            serverChannel.close().awaitUninterruptibly();
            break;

        case CLIENT_SERVER_DISCONNECTED:
            ClientGlobal.setIsMasterAlive(false);
            reconnect();
            break;

        case CLIENT_SERVER_HEARTBEAT:
            synchronizedSet(timeoutTime, timeoutValue);
            break;
        default:
            throw new UnrecognizedEvent(event.toString());
        }
    }

    protected void synchronizedSet(AtomicLong timeoutTime, long value) {
        synchronized (timeoutTime) {
            timeoutTime.set(value);
        }
    }

    protected void startHeartBeat() {
        worker = new HeartBeatWorker(serverChannel);
        worker.start();
    }

    private void reconnect() {
        if (worker != null) {
            worker.stop();
        }

        while (true) {
            logger.info("START RECONNECTING......");
            // Make sure next acquire get the permit
            sep.release();

            isConnected = false;
            // Enter the door
            sep.acquireUninterruptibly();

            ChannelFuture f = bootstrap.connect();
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        isConnected = true;
                    }

                    sep.release();
                }
            });

            // Get the permit
            sep.acquireUninterruptibly();

            if (isConnected) {
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
