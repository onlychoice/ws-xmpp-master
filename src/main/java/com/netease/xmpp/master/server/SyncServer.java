package com.netease.xmpp.master.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.netease.xmpp.hash.server.HashFileMonitor;
import com.netease.xmpp.master.common.ConfigConst;
import com.netease.xmpp.master.common.MessageDecoder;
import com.netease.xmpp.master.event.EventDispatcher;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.server.ClientHearBeatEventHandler;
import com.netease.xmpp.master.event.server.ClientSyncedEventHandler;
import com.netease.xmpp.master.event.server.HashAlgorithmEventHandler;
import com.netease.xmpp.master.event.server.ProxyEventHandler;
import com.netease.xmpp.master.event.server.RobotEventHandler;
import com.netease.xmpp.master.event.server.XmppServerEventHandler;
import com.netease.xmpp.util.ResourceUtils;

/**
 * The master server.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public class SyncServer {
    /**
     * File name for config.
     */
    private static final String CONFIG_FILE = "master.properties";

    /**
     * Loading server config.
     * 
     * @param config
     *            the config object
     * @return true if loading successfully, false otherwise
     */
    private boolean loadConfig(ServerConfigCache config) {
        try {
            Properties prop = new Properties();
            InputStream input = ResourceUtils.getResourceAsStream(CONFIG_FILE);

            prop.load(input);

            config.setHashAlgorithmClassName(prop.getProperty(ConfigConst.KEY_HASH_CLASS_NAME,
                    ConfigConst.DEFAULT_HASH_CLASS_NAME));
            String serverRepNum = prop.getProperty(ConfigConst.KEY_SERVER_REP_NUM, String
                    .valueOf(ConfigConst.DEFAULT_SERVER_REP_NUM));
            config.setServerRepNum(Integer.valueOf(serverRepNum));

            String xmppServerPort = prop.getProperty(ConfigConst.KEY_XMPP_SERVER_PORT, String
                    .valueOf(ConfigConst.DEFAULT_XMPP_SERVER_PORT));
            config.setXmppServerPort(Integer.valueOf(xmppServerPort));

            String proxyPort = prop.getProperty(ConfigConst.KEY_PROXY_PORT, String
                    .valueOf(ConfigConst.DEFAULT_PROXY_PORT));
            config.setProxyPort(Integer.valueOf(proxyPort));

            String robotPort = prop.getProperty(ConfigConst.KEY_ROBOT_PORT, String
                    .valueOf(ConfigConst.DEFAULT_ROBOT_PORT));
            config.setRobotPort(Integer.valueOf(robotPort));

            String hashFilePath = prop.getProperty(ConfigConst.KEY_HASH_FILE_PATH, String
                    .valueOf(ConfigConst.DEFAULT_HASH_FILE_PATH));
            config.setHashFilePath(hashFilePath);

            String serverMonitorInterval = prop.getProperty(
                    ConfigConst.KEY_SERVER_MONITOR_INTERVAL, String
                            .valueOf(ConfigConst.DEFAULT_SERVER_MONITOR_INTERVAL));
            config.setServerMonitorInterval(Integer.valueOf(serverMonitorInterval));

            String hashMonitorInterval = prop.getProperty(ConfigConst.KEY_HASH_MONITOR_INTERVAL,
                    String.valueOf(ConfigConst.DEFAULT_HASH_MONITOR_INTERVAL));
            config.setHashMonitorInterval(Integer.valueOf(hashMonitorInterval));

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Start the master server.
     */
    public void start() {
        ServerConfigCache serverConfig = ServerConfigCache.getInstance();

        if (!loadConfig(serverConfig)) {
            return;
        }

        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("child.tcpNoDelay", true);
        config.put("child.keepAlive", true);

        final EventDispatcher eventDispatcher = EventDispatcher.getInstance();
        ClientCache clientCache = ClientCache.getInstance();

        XmppServerEventHandler xmppServerEventHandler = new XmppServerEventHandler(ClientCache
                .getInstance());
        ProxyEventHandler proxyEventHandler = new ProxyEventHandler(clientCache);
        RobotEventHandler robotEventHandler = new RobotEventHandler(clientCache);
        HashAlgorithmEventHandler hashEventHandler = new HashAlgorithmEventHandler(serverConfig,
                clientCache);
        ClientSyncedEventHandler clientSyncedEventHandler = new ClientSyncedEventHandler(
                clientCache);
        ClientHearBeatEventHandler clientHeartBeatEventHandler = new ClientHearBeatEventHandler(
                eventDispatcher);

        eventDispatcher.registerEvent(clientSyncedEventHandler, EventType.SERVER_SYNCED,
                EventType.HASH_SYNCED);

        // Xmpp server event handler
        eventDispatcher.registerEvent(xmppServerEventHandler, //
                EventType.SERVER_CONNECT, //
                EventType.SERVER_INFO_RECV, //
                EventType.SERVER_DISCONNECT, //
                EventType.SERVER_HEARTBEAT, //
                EventType.SERVER_HEARTBEAT_TIMEOUT);

        // Proxy event handler
        eventDispatcher.registerEvent(proxyEventHandler, //
                EventType.PROXY_CONNECT, //
                EventType.PROXY_DISCONNECT, // 
                EventType.PROXY_EXCEPTION, //
                EventType.PROXY_HEARTBEAT, //
                EventType.PROXY_HEARTBEAT_TIMEOUT, //
                EventType.PROXY_SERVER_UPDATE_COMPLETE, // 
                EventType.PROXY_HASH_UPDATE_COMPLETE);

        // Robot event handler
        eventDispatcher.registerEvent(robotEventHandler, //
                EventType.ROBOT_CONNECT, //
                EventType.ROBOT_DISCONNECT, //
                EventType.ROBOT_EXCEPTION, //
                EventType.ROBOT_HEARTBEAT, //
                EventType.ROBOT_HEARTBEAT_TIMEOUT, //
                EventType.ROBOT_SERVER_UPDATE_COMPLETE, //
                EventType.ROBOT_HASH_UPDATE_COMPLETE);

        // Hash algorithm update event handler
        eventDispatcher.registerEvent(hashEventHandler, EventType.HASH_UPDATED);

        // Client heart beat event handler
        eventDispatcher.registerEvent(clientHeartBeatEventHandler, //
                EventType.SERVER_HEARTBEAT, //
                EventType.SERVER_HEARTBEAT_START, //
                EventType.SERVER_HEARTBEAT_STOP, //

                EventType.PROXY_HEARTBEAT, //
                EventType.PROXY_HEARTBEAT_START, //
                EventType.PROXY_HEARTBEAT_STOP, //

                EventType.ROBOT_HEARTBEAT, //
                EventType.ROBOT_HEARTBEAT_START, //
                EventType.ROBOT_HEARTBEAT_STOP);

        // XMPP Server channel
        ServerBootstrap xmppServerBootstrap = new ServerBootstrap(factory);
        xmppServerBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new MessageDecoder(), new CommonClientChannelHandler(
                        eventDispatcher, ConfigConst.CLIENT_XMPP_SERVER));
            }
        });

        xmppServerBootstrap.setOptions(config);
        xmppServerBootstrap.bind(new InetSocketAddress(serverConfig.getXmppServerPort()));

        // Proxy channel
        ServerBootstrap proxyBootstrap = new ServerBootstrap(factory);
        proxyBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new MessageDecoder(), new CommonClientChannelHandler(
                        eventDispatcher, ConfigConst.CLIENT_PROXY));
            }
        });

        proxyBootstrap.setOptions(config);
        proxyBootstrap.bind(new InetSocketAddress(serverConfig.getProxyPort()));

        // Robot channel
        ServerBootstrap robotBootstrap = new ServerBootstrap(factory);
        robotBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new MessageDecoder(), new CommonClientChannelHandler(
                        eventDispatcher, ConfigConst.CLIENT_ROBOT));
            }
        });

        robotBootstrap.setOptions(config);
        robotBootstrap.bind(new InetSocketAddress(serverConfig.getRobotPort()));

        // Start hash file monitor
        new Thread(new HashFileMonitor(eventDispatcher, serverConfig)).start();

        // Client state monitor
        new Thread(new ClientStateMonitor(clientCache, serverConfig)).start();

        System.out.println("Server startup at: XMPP_PORT=" + serverConfig.getXmppServerPort()
                + ", PROXY_PORT=" + serverConfig.getProxyPort() + ", ROBOT_PORT="
                + serverConfig.getRobotPort());

    }

    public static void main(String[] args) throws Exception {
        new SyncServer().start();
    }
}
