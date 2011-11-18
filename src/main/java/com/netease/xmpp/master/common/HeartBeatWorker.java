package com.netease.xmpp.master.common;

import org.jboss.netty.channel.Channel;

public class HeartBeatWorker {
    private static final Message HEATBEAT = new Message(MessageFlag.FLAG_HEATBEAT, 0, 0, null);

    private Channel channel = null;
    private Thread workerThread = null;

    public HeartBeatWorker(Channel channel) {
        this.channel = channel;
        workerThread = new Thread(new Worker());
    }

    public void start() {
        workerThread.start();
    }

    public void stop() {
        try {
            workerThread.interrupt();
            workerThread.join();
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    class Worker implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(ConfigConst.HEARTBEAT_INTERVAL * 1000);

                    channel.write(HEATBEAT);
                }
            } catch (InterruptedException e) {
                // Exit thread
            }
        }
    }
}
