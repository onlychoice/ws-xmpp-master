package com.netease.xmpp.master.client;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.netease.xmpp.master.common.ServerListProtos.Server.ServerInfo;

public class ClientGlobal {

    /**
     * Is proxy startup?
     */
    private static AtomicBoolean isClientStartup = new AtomicBoolean(false);

    /**
     * Is server info update completely?
     */
    private static AtomicBoolean isServerUpdate = new AtomicBoolean(false);

    /**
     * Is hash info update completely?
     */
    private static AtomicBoolean isHashUpdate = new AtomicBoolean(false);

    /**
     * Is all client synced to the latest server info?
     */
    private static AtomicBoolean isAllServerUpdate = new AtomicBoolean(false);

    /**
     * Is all client synced to the latest hash info?
     */
    private static AtomicBoolean isAllHashUpdate = new AtomicBoolean(false);
    
    private static TreeMap<Long, ServerInfo> serverNodes = new TreeMap<Long, ServerInfo>();

    public static boolean getIsClientStartup() {
        return isClientStartup.get();
    }

    public static void setIsClientStartup(boolean flag) {
        isClientStartup.set(flag);
    }

    public static boolean getIsServerUpdate() {
        return isServerUpdate.get();
    }

    public static void setIsServerUpdate(boolean flag) {
        isServerUpdate.set(flag);
    }

    public static boolean getIsHashUpdate() {
        return isHashUpdate.get();
    }

    public static void setIsHashUpdate(boolean flag) {
        isHashUpdate.set(flag);
    }

    public static boolean getIsAllServerUpdate() {
        return isAllServerUpdate.get();
    }

    public static void setIsAllServerUpdate(boolean flag) {
        isAllServerUpdate.set(flag);
    }

    public static boolean getIsAllHashUpdate() {
        return isAllHashUpdate.get();
    }

    public static void setIsAllHashUpdate(boolean flag) {
        isAllHashUpdate.set(flag);
    }

    public static synchronized boolean getIsUpdating() {
        if (getIsAllServerUpdate() && getIsAllHashUpdate()) {
            return false;
        }

        return true;
    }
    
    public static TreeMap<Long, ServerInfo> getServerNodes() {
        return serverNodes;
    }

    public static void setServerNodes(TreeMap<Long, ServerInfo> serverNodes) {
        ClientGlobal.serverNodes = serverNodes;
    }

    public static ServerInfo getServerNodeForKey(Long key) {
        if (!serverNodes.containsKey(key)) {
            key = serverNodes.ceilingKey(key);
            if (key == null) {
                key = serverNodes.firstKey();
            }
        }

        return serverNodes.get(key);
    }
}
