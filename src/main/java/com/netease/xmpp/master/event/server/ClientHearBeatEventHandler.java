package com.netease.xmpp.master.event.server;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.netease.xmpp.master.common.ConfigConst;
import com.netease.xmpp.master.common.HeartBeatWorker;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventDispatcher;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;

public class ClientHearBeatEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(ClientHearBeatEventHandler.class);

    private Map<Channel, HeartBeatWorker> heartBeatWorkerMap = new ConcurrentHashMap<Channel, HeartBeatWorker>();

    private Map<Channel, Long> xmppServerHeartBeatTimeoutMap = new ConcurrentHashMap<Channel, Long>();

    private Map<Channel, Long> proxyHeartBeatTimeoutMap = new ConcurrentHashMap<Channel, Long>();

    private Map<Channel, Long> robotHeartBeatTimeoutMap = new ConcurrentHashMap<Channel, Long>();

    private EventDispatcher eventDispatcher = null;

    private ThreadPoolExecutor threadPool;

    private Thread[] timeoutChecker = new Thread[3];

    class TimeoutWorker implements Runnable {
        private Map<Channel, Long> timeoutMap = null;
        private String clientPrefix = null;

        public TimeoutWorker(Map<Channel, Long> timeoutMap, String clientPrefix) {
            this.timeoutMap = timeoutMap;
            this.clientPrefix = clientPrefix;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (timeoutMap) {
                    if (timeoutMap.size() > 0) {
                        for (Entry<Channel, Long> entry : timeoutMap.entrySet()) {
                            if (entry.getValue() <= System.currentTimeMillis()) {
                                logger.debug(clientPrefix + "_HEARTBEAT_TIMEOUT: "
                                        + entry.getKey().getRemoteAddress());
                                eventDispatcher.dispatchEvent(entry.getKey(), null, EventType
                                        .valueOf(clientPrefix + "_HEARTBEAT_TIMEOUT"));
                            }
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
    }

    class HeartBeatWorkerStoper implements Runnable {
        private HeartBeatWorker worker;

        public HeartBeatWorkerStoper(HeartBeatWorker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            worker.stop();
        }
    }

    public ClientHearBeatEventHandler(EventDispatcher dispatcher) {
        this.eventDispatcher = dispatcher;
        timeoutChecker[0] = new Thread(new TimeoutWorker(xmppServerHeartBeatTimeoutMap,
                ConfigConst.CLIENT_XMPP_SERVER));
        timeoutChecker[1] = new Thread(new TimeoutWorker(proxyHeartBeatTimeoutMap,
                ConfigConst.CLIENT_PROXY));
        timeoutChecker[2] = new Thread(new TimeoutWorker(robotHeartBeatTimeoutMap,
                ConfigConst.CLIENT_ROBOT));

        for (int i = 0; i < timeoutChecker.length; i++) {
            timeoutChecker[i].start();
        }

        threadPool = new ThreadPoolExecutor(3, 3, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        Channel channel = ctx.getChannel();
        EventType event = ctx.getEvent();

        long timeoutTime = System.currentTimeMillis() + ConfigConst.HEARTBEAT_TIMEOUT * 1000;

        switch (event) {
        /**
         * Start heart beat
         */
        case SERVER_HEARTBEAT_START:
            synchronizedPut(xmppServerHeartBeatTimeoutMap, channel, timeoutTime);
            startHeartBeat(channel);
            break;
        case PROXY_HEARTBEAT_START:
            synchronizedPut(proxyHeartBeatTimeoutMap, channel, timeoutTime);
            startHeartBeat(channel);
            break;
        case ROBOT_HEARTBEAT_START:
            synchronizedPut(robotHeartBeatTimeoutMap, channel, timeoutTime);
            startHeartBeat(channel);
            break;

        /**
         * Stop heart beat
         */
        case SERVER_HEARTBEAT_STOP:
            synchronizedRemove(xmppServerHeartBeatTimeoutMap, channel);
            stopHeartBeat(channel);
            break;
        case PROXY_HEARTBEAT_STOP:
            synchronizedRemove(proxyHeartBeatTimeoutMap, channel);
            stopHeartBeat(channel);
            break;
        case ROBOT_HEARTBEAT_STOP:
            synchronizedRemove(robotHeartBeatTimeoutMap, channel);
            stopHeartBeat(channel);
            break;

        /**
         * Normal heart beat
         */
        case SERVER_HEARTBEAT:
            synchronizedPut(xmppServerHeartBeatTimeoutMap, channel, timeoutTime);
            break;
        case PROXY_HEARTBEAT:
            synchronizedPut(proxyHeartBeatTimeoutMap, channel, timeoutTime);
            break;
        case ROBOT_HEARTBEAT:
            synchronizedPut(robotHeartBeatTimeoutMap, channel, timeoutTime);
            break;
        default:
            throw new UnrecognizedEvent(event.toString());
        }
    }

    private void startHeartBeat(Channel channel) {
        HeartBeatWorker worker = new HeartBeatWorker(channel);
        heartBeatWorkerMap.put(channel, worker);
        worker.start();
    }

    private void stopHeartBeat(Channel channel) {
        HeartBeatWorker worker = heartBeatWorkerMap.get(channel);
        if (worker != null) {
            threadPool.execute(new HeartBeatWorkerStoper(worker));
        }
        heartBeatWorkerMap.remove(channel);
    }

    private void synchronizedPut(Map<Channel, Long> timeoutMap, Channel channel, long time) {
        synchronized (timeoutMap) {
            timeoutMap.put(channel, time);
        }
    }

    private void synchronizedRemove(Map<Channel, Long> timeoutMap, Channel channel) {
        synchronized (timeoutMap) {
            timeoutMap.remove(channel);
        }
    }
}
