package com.netease.xmpp.master.client;

import java.util.TreeMap;

import com.netease.xmpp.master.common.ServerListProtos.Server.ServerInfo;

public class ClientGlobal {

    /**
     * Is proxy startup?
     */
    private static volatile boolean isClientStarted = false;

    /**
     * Is server info update completely?
     */
    private static volatile boolean isServerUpdated = false;

    /**
     * Is hash info update completely?
     */
    private static volatile boolean isHashUpdated = false;

    /**
     * Is all client synced to the latest server info?
     */
    private static volatile boolean isAllServerUpdated = false;

    /**
     * Is all client synced to the latest hash info?
     */
    private static volatile boolean isAllHashUpdated = false;

    private static TreeMap<Long, ServerInfo> serverNodes = new TreeMap<Long, ServerInfo>();

    private static volatile boolean isMasterAlive = false;

    public static boolean getIsClientStarted() {
        return isClientStarted;
    }

    public static void setIsClientStarted(boolean flag) {
        isClientStarted = flag;
    }

    public static boolean getIsServerUpdated() {
        return isServerUpdated;
    }

    public static void setIsServerUpdated(boolean flag) {
        isServerUpdated = flag;
    }

    public static boolean getIsHashUpdated() {
        return isHashUpdated;
    }

    public static void setIsHashUpdated(boolean flag) {
        isHashUpdated = flag;
    }

    public static boolean getIsAllServerUpdated() {
        return isAllServerUpdated;
    }

    public static void setIsAllServerUpdated(boolean flag) {
        isAllServerUpdated = flag;
    }

    public static boolean getIsAllHashUpdated() {
        return isAllHashUpdated;
    }

    public static void setIsAllHashUpdated(boolean flag) {
        isAllHashUpdated = flag;
    }

    public static boolean getIsMasterAlive() {
        return isMasterAlive;
    }

    public static void setIsMasterAlive(boolean flag) {
        isMasterAlive = flag;
    }

    public static synchronized boolean getIsUpdating() {
        if (getIsAllServerUpdated() && getIsAllHashUpdated()) {
            return false;
        }

        return true;
    }

    public static synchronized TreeMap<Long, ServerInfo> getServerNodes() {
        return serverNodes;
    }

    public static synchronized void setServerNodes(TreeMap<Long, ServerInfo> serverNodes) {
        ClientGlobal.serverNodes = serverNodes;
    }

    public static synchronized ServerInfo getServerNodeForKey(Long key) {
        if (!serverNodes.containsKey(key)) {
            key = serverNodes.ceilingKey(key);
            if (key == null) {
                key = serverNodes.firstKey();
            }
        }

        return serverNodes.get(key);
    }
}
