package com.netease.xmpp.master.server;

import java.util.List;

import org.jboss.netty.channel.Channel;

/**
 * Monitor for master server.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public class ClientStateMonitor implements Runnable {
    private ClientCache clientCache = null;
    private int interval = 0;

    public ClientStateMonitor(ClientCache clientCache, ServerConfigCache serverConfig) {
        this.clientCache = clientCache;
        this.interval = serverConfig.getServerMonitorInterval();
    }

    @Override
    public void run() {
        while (true) {
            List<Channel> serverList = clientCache.getXmppServerList();
            List<Channel> proxyList = clientCache.getProxyList();
            List<Channel> robotList = clientCache.getRobotList();

            System.out.println("SERVER SIZE=" + serverList.size() + ", PROXY SIZE="
                    + proxyList.size() + ", ROBOT SIZE=" + robotList.size());

            System.out.format("%10s%10s%10s%10s\n", " ", "VERSION", "PROXY", "ROBOT");
            System.out.format("%10s%10d%10d%10d\n", "SERVER", clientCache.getServerVersion(),
                    clientCache.getAllProxyServerSync(), clientCache.getAllRobotServerSync());
            System.out.format("%10s%10d%10d%10d\n", "HASH", clientCache.getHashVersion(),
                    clientCache.getAllProxyHashSync(), clientCache.getAllRobotHashSync());
            System.out.format("%40s\n\n", "========================================");

            try {
                Thread.sleep(interval * 1000);
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
    }
}
