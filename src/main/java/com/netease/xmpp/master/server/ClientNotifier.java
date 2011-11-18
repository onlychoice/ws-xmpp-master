package com.netease.xmpp.master.server;

import java.io.IOException;
import java.util.List;

import org.jboss.netty.channel.Channel;

import com.netease.xmpp.hash.server.KetamaNodeLocator;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.common.MessageFlag;
import com.netease.xmpp.master.common.ServerListProtos.Server.ServerInfo;

/**
 * Notifier for client with updated info.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public final class ClientNotifier {
    /**
     * Notify specific client with the updated server info.
     * 
     * @param channel
     *            client
     * @throws IOException
     *             throws when build server hash info
     */
    public static void notifyServerUpdate(Channel channel) throws IOException {
        ClientCache clientCache = ClientCache.getInstance();
        ServerConfigCache configCache = ServerConfigCache.getInstance();
        KetamaNodeLocator locator = new KetamaNodeLocator(clientCache.getXmppServerList(),
                configCache, MessageFlag.FLAG_SERVER_ALL);

        byte[] serverHash = locator.getServerHashList();

        Message message = new Message(MessageFlag.FLAG_SERVER_UPDATED, clientCache
                .getServerVersion(), serverHash.length, serverHash);

        channel.write(message);
    }

    /**
     * Server added.
     * 
     * @param server
     *            the server added
     * @throws IOException
     *             throws when build server hash info
     */
    public static void notifyAllServerAdded(ServerInfo server) throws IOException {
        notifyAllServerUpdate(server, MessageFlag.FLAG_SERVER_ADD);
    }

    /**
     * Server Down
     * 
     * @param server
     *            the server down
     * @throws IOException
     *             throws when build server hash info
     */
    public static void notifyAllServerDown(ServerInfo server) throws IOException {
        notifyAllServerUpdate(server, MessageFlag.FLAG_SERVER_DEL);
    }

    /**
     * Notify all client with the updated server info.
     * 
     * @throws IOException
     *             throws when build server hash info
     */
    private static void notifyAllServerUpdate(ServerInfo server, int infoFlag) throws IOException {
        ClientCache clientCache = ClientCache.getInstance();
        ServerConfigCache configCache = ServerConfigCache.getInstance();

        clientCache.clearAllProxyServerSync();
        clientCache.clearAllRobotServerSync();

        KetamaNodeLocator locator = new KetamaNodeLocator(clientCache.getXmppServerList(),
                configCache, infoFlag);

        byte[] serverHash = locator.getServerHashList();

        Message message = new Message(MessageFlag.FLAG_SERVER_UPDATED, clientCache
                .getServerVersion(), serverHash.length, serverHash);

        List<Channel> proxyList = clientCache.getProxyList();

        for (Channel ch : proxyList) {
            ch.write(message);
        }

        List<Channel> robotList = clientCache.getRobotList();
        for (Channel ch : robotList) {
            ch.write(message);
        }
    }

    /**
     * Notify specific client with the updated hash algorithm.
     * 
     * @param channel
     *            client
     */
    public static void notifyHashUpdate(Channel channel) {
        ServerConfigCache configCache = ServerConfigCache.getInstance();
        ClientCache clientCache = ClientCache.getInstance();

        byte[] hashData = configCache.getHashAlgorithmCode();
        Message message = new Message(MessageFlag.FLAG_HASH_UPDATED, clientCache.getHashVersion(),
                hashData.length, hashData);
        channel.write(message);
    }

    /**
     * Notify all client with the updated hash algorithm.
     */
    public static void notifyAllHashUpdate() {
        ClientCache clientCache = ClientCache.getInstance();
        ServerConfigCache configCache = ServerConfigCache.getInstance();

        clientCache.clearAllProxyHashSync();
        clientCache.clearAllRobotHashSync();

        byte[] hashData = configCache.getHashAlgorithmCode();
        Message message = new Message(MessageFlag.FLAG_HASH_UPDATED, clientCache.getHashVersion(),
                hashData.length, hashData);

        List<Channel> proxyList = clientCache.getProxyList();
        for (Channel ch : proxyList) {
            ch.write(message);
        }

        List<Channel> robotList = clientCache.getRobotList();
        for (Channel ch : robotList) {
            ch.write(message);
        }
    }

    /**
     * Notify all client that server info have been synced on all client to the latest version.
     */
    public static void notifyAllServerSyncCompleted() {
        ClientCache clientCache = ClientCache.getInstance();
        Message message = new Message(MessageFlag.FLAG_SERVER_ALL_COMPLETE, clientCache
                .getServerVersion(), 0, null);

        List<Channel> proxyList = clientCache.getProxyList();
        for (Channel ch : proxyList) {
            ch.write(message);
        }

        List<Channel> robotList = clientCache.getRobotList();
        for (Channel ch : robotList) {
            ch.write(message);
        }
    }

    /**
     * Notify all client that hash algorithm have been synced on all client to the latest version.
     */
    public static void notifyAllHashSyncCompleted() {
        ClientCache clientCache = ClientCache.getInstance();
        Message message = new Message(MessageFlag.FLAG_HASH_ALL_COMPLETE, clientCache
                .getHashVersion(), 0, null);

        List<Channel> proxyList = clientCache.getProxyList();
        for (Channel ch : proxyList) {
            ch.write(message);
        }

        List<Channel> robotList = clientCache.getRobotList();
        for (Channel ch : robotList) {
            ch.write(message);
        }
    }
}
