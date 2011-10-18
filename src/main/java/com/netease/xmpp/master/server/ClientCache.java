package com.netease.xmpp.master.server;

import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;

/**
 * Cache for client.
 * <p>
 * Include following content:
 * <p>
 * 1. XMPP Server list; 2. Proxy list; 3. Robot list; 4. Sync status.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public class ClientCache {
    /**
     * XMPP Server list.
     */
    private List<Channel> xmppServerList = Collections.synchronizedList(new LinkedList<Channel>());
    /**
     * Proxy list.
     */
    private List<Channel> proxyList = Collections.synchronizedList(new LinkedList<Channel>());
    /**
     * Robot list.
     */
    private List<Channel> robotList = Collections.synchronizedList(new LinkedList<Channel>());

    /**
     * Current server version.
     */
    private AtomicInteger serverVersion = new AtomicInteger(1);
    /**
     * Current hash algorithm version.
     */
    private AtomicInteger hashVersion = new AtomicInteger(1);

    /**
     * Sync status for server info on proxy.
     */
    private BitSet proxyServerSyncStatus = new BitSet();
    /**
     * Sync status for hash algorithm info on proxy.
     */
    private BitSet proxyHashSyncStatus = new BitSet();

    /**
     * Sync status for server info on robot.
     */
    private BitSet robotServerSyncStatus = new BitSet();
    /**
     * Sync status for hash algorithm info on robot.
     */
    private BitSet robotHashSyncStatus = new BitSet();

    /**
     * The singleton cache instance.
     */
    private static ClientCache instance = null;

    public static ClientCache getInstance() {
        if (instance == null) {
            instance = new ClientCache();
        }

        return instance;
    }

    private ClientCache() {
        // Do nothing
    }

    public int incrServerVersion() {
        return serverVersion.incrementAndGet();
    }

    public int getServerVersion() {
        return serverVersion.intValue();
    }

    public int incrHashVersion() {
        return hashVersion.incrementAndGet();
    }

    public int getHashVersion() {
        return hashVersion.intValue();
    }

    public synchronized void addXmppServer(Channel ch) {
        xmppServerList.add(ch);
        serverVersion.incrementAndGet();
    }

    public synchronized void removeXmppServer(Channel ch) {
        xmppServerList.remove(ch);
        serverVersion.incrementAndGet();
    }

    public List<Channel> getXmppServerList() {
        return xmppServerList;
    }

    public void addProxy(Channel ch) {
        proxyList.add(ch);

        setProxyServerSync(ch, false);
        setProxyHashSync(ch, false);
    }

    public void removeProxy(Channel ch) {
        removeProxySync(ch);

        proxyList.remove(ch);
    }

    public List<Channel> getProxyList() {
        return proxyList;
    }

    public void addRobot(Channel ch) {
        robotList.add(ch);

        setRobotServerSync(ch, false);
        setRobotHashSync(ch, false);
    }

    public void removeRobot(Channel ch) {
        removeRobotSync(ch);

        robotList.remove(ch);
    }

    public List<Channel> getRobotList() {
        return robotList;
    }

    public void setProxyServerSync(Channel channel, boolean value) {
        int index = proxyList.indexOf(channel);

        synchronized (proxyServerSyncStatus) {
            proxyServerSyncStatus.set(index, value);
        }
    }

    public boolean getProxyServerSync(Channel channel) {
        int index = proxyList.indexOf(channel);

        synchronized (proxyServerSyncStatus) {
            return proxyServerSyncStatus.get(index);
        }
    }

    public void setProxyHashSync(Channel channel, boolean value) {
        int index = proxyList.indexOf(channel);

        synchronized (proxyHashSyncStatus) {
            proxyHashSyncStatus.set(index, value);
        }
    }

    public boolean getProxyHashSync(Channel channel) {
        int index = proxyList.indexOf(channel);

        synchronized (proxyHashSyncStatus) {
            return proxyHashSyncStatus.get(index);
        }
    }

    public void removeProxySync(Channel channel) {
        int length = proxyList.size();
        if(length == 0) {
            return;
        }
        
        int index = proxyList.indexOf(channel);
        if(index < 0) {
            return;
        }
        
        if (index >= length - 1) {
            synchronized (proxyServerSyncStatus) {
                proxyServerSyncStatus.set(index, false);
            }

            synchronized (proxyHashSyncStatus) {
                proxyHashSyncStatus.set(index, false);
            }
            return;
        }

        synchronized (proxyServerSyncStatus) {
            BitSet tmp = proxyServerSyncStatus.get(index + 1, length);
            proxyServerSyncStatus.set(index, length, false);
            for (int i = 0; i < tmp.length(); i++) {
                proxyServerSyncStatus.set(index + i, tmp.get(i));
            }
        }

        synchronized (proxyHashSyncStatus) {
            BitSet tmp = proxyHashSyncStatus.get(index + 1, length);
            proxyHashSyncStatus.set(index, length, false);
            for (int i = 0; i < tmp.length(); i++) {
                proxyHashSyncStatus.set(index + i, tmp.get(i));
            }
        }
    }

    public void clearAllProxyServerSync() {
        synchronized (proxyServerSyncStatus) {
            proxyServerSyncStatus.clear();
        }
    }

    public int getAllProxyServerSync() {
        synchronized (proxyServerSyncStatus) {
            return proxyServerSyncStatus.cardinality();
        }
    }

    public void clearAllProxyHashSync() {
        synchronized (proxyHashSyncStatus) {
            proxyHashSyncStatus.clear();
        }
    }

    public int getAllProxyHashSync() {
        synchronized (proxyHashSyncStatus) {
            return proxyHashSyncStatus.cardinality();
        }
    }

    public void setRobotServerSync(Channel channel, boolean value) {
        int index = robotList.indexOf(channel);

        synchronized (robotServerSyncStatus) {
            robotServerSyncStatus.set(index, value);
        }
    }

    public boolean getRobotServerSync(Channel channel) {
        int index = robotList.indexOf(channel);

        synchronized (robotServerSyncStatus) {
            return robotServerSyncStatus.get(index);
        }
    }

    public void setRobotHashSync(Channel channel, boolean value) {
        int index = robotList.indexOf(channel);

        synchronized (robotHashSyncStatus) {
            robotHashSyncStatus.set(index, value);
        }
    }

    public boolean getRobotHashSync(Channel channel) {
        int index = robotList.indexOf(channel);

        synchronized (robotHashSyncStatus) {
            return robotHashSyncStatus.get(index);
        }
    }

    public void removeRobotSync(Channel channel) {
        int length = robotList.size();
        if(length == 0) {
            return;
        }
        
        int index = robotList.indexOf(channel);
        if(index < 0) {
            return;
        }
        
        if (index >= length - 1) {
            synchronized (robotServerSyncStatus) {
                robotServerSyncStatus.set(index, false);
            }

            synchronized (robotHashSyncStatus) {
                robotHashSyncStatus.set(index, false);
            }
            return;
        }

        synchronized (robotServerSyncStatus) {
            BitSet tmp = robotServerSyncStatus.get(index + 1, length);
            robotServerSyncStatus.set(index, length, false);
            for (int i = 0; i < tmp.length(); i++) {
                robotServerSyncStatus.set(index + i, tmp.get(i));
            }
        }

        synchronized (robotHashSyncStatus) {
            BitSet tmp = robotHashSyncStatus.get(index + 1, length);
            robotHashSyncStatus.set(index, length, false);
            for (int i = 0; i < tmp.length(); i++) {
                robotHashSyncStatus.set(index + i, tmp.get(i));
            }
        }
    }

    public void clearAllRobotServerSync() {
        synchronized (robotServerSyncStatus) {
            robotServerSyncStatus.clear();
        }
    }

    public int getAllRobotServerSync() {
        synchronized (robotServerSyncStatus) {
            return robotServerSyncStatus.cardinality();
        }
    }

    public void clearAllRobotHashSync() {
        synchronized (robotHashSyncStatus) {
            robotHashSyncStatus.clear();
        }
    }

    public int getAllRobotHashSync() {
        synchronized (robotHashSyncStatus) {
            return robotHashSyncStatus.cardinality();
        }
    }
}
