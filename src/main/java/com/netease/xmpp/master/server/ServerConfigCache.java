package com.netease.xmpp.master.server;

import com.netease.xmpp.master.common.ConfigCache;
import com.netease.xmpp.master.common.ConfigConst;

/**
 * Config for master server.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public class ServerConfigCache extends ConfigCache {
    /**
     * The singleton config instance.
     */
    private static ServerConfigCache instance = null;

    public static ServerConfigCache getInstance() {
        if (instance == null) {
            instance = new ServerConfigCache();
        }

        return instance;
    }

    /**
     * Replication number for server hash.
     */
    private int serverRepNum = ConfigConst.DEFAULT_SERVER_REP_NUM;

    /**
     * Interval for master server monitoring.
     */
    private int serverMonitorInterval = ConfigConst.DEFAULT_SERVER_MONITOR_INTERVAL;

    /**
     * Java class code for hash algorithm implementation.
     */
    private byte[] hashAlgorithmCode = null;

    /**
     * File path for hash algorithm implementation.
     */
    private String hashFilePath = null;

    /**
     * Interval for hash algorithm implementation file monitoring.
     */
    private int hashMonitorInterval = ConfigConst.DEFAULT_HASH_MONITOR_INTERVAL;

    /**
     * Binding port for xmpp server client.
     */
    private int xmppServerPort = ConfigConst.DEFAULT_XMPP_SERVER_PORT;

    /**
     * Binding port for proxy client.
     */
    private int proxyPort = ConfigConst.DEFAULT_PROXY_PORT;

    /**
     * Binding port for robot client.
     */
    private int robotPort = ConfigConst.DEFAULT_ROBOT_PORT;

    private ServerConfigCache() {
        // Do nothing
    }

    public int getServerRepNum() {
        return serverRepNum;
    }

    public void setServerRepNum(int serverRepNum) {
        this.serverRepNum = serverRepNum;
    }

    public int getServerMonitorInterval() {
        return serverMonitorInterval;
    }

    public void setServerMonitorInterval(int serverMonitorInterval) {
        this.serverMonitorInterval = serverMonitorInterval;
    }

    public synchronized byte[] getHashAlgorithmCode() {
        return hashAlgorithmCode;
    }

    public synchronized void setHashAlgorithmCode(byte[] code) {
        this.hashAlgorithmCode = code;
    }

    public String getHashFilePath() {
        return hashFilePath;
    }

    public void setHashFilePath(String hashFilePath) {
        this.hashFilePath = hashFilePath;
    }

    public int getHashMonitorInterval() {
        return hashMonitorInterval;
    }

    public void setHashMonitorInterval(int hashMonitorInterval) {
        this.hashMonitorInterval = hashMonitorInterval;
    }

    public int getXmppServerPort() {
        return xmppServerPort;
    }

    public void setXmppServerPort(int xmppServerPort) {
        this.xmppServerPort = xmppServerPort;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public int getRobotPort() {
        return robotPort;
    }

    public void setRobotPort(int robotPort) {
        this.robotPort = robotPort;
    }
}
