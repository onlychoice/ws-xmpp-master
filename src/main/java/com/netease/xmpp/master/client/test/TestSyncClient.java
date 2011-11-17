package com.netease.xmpp.master.client.test;

import java.util.List;

import org.jboss.netty.bootstrap.ClientBootstrap;

import com.netease.xmpp.master.client.ClientConfigCache;
import com.netease.xmpp.master.client.SyncClient;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.common.ServerListProtos.Server.ServerInfo;
import com.netease.xmpp.master.event.EventDispatcher;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.client.HashUpdateEventHandler;
import com.netease.xmpp.master.event.client.ServerConnectionEventHandler;
import com.netease.xmpp.master.event.client.ServerUpdateEventHandler;
import com.netease.xmpp.master.event.client.UpdateCompletedEventHandler;

public class TestSyncClient extends SyncClient {
    class TestServerUpdateEventHandler extends ServerUpdateEventHandler {
        public TestServerUpdateEventHandler(ClientConfigCache config) {
            super(config);
        }

        @Override
        public void serverInfoUpdated(Message data, List<ServerInfo> serverHashList) {
            System.out.println("Server version: " + data.getVersion() + ", server size: "
                    + serverHashList.size());
        }
    }

    class TestUpdateCompletedEventHandler extends UpdateCompletedEventHandler {
        @Override
        public void allHashUpdated() {
            System.out.println("Hash updated.");
        }

        @Override
        public void allServerUpdated() {
            System.out.println("Server updated.");
        }
    }

    public TestSyncClient(int clientType) {
        super(clientType);
    }

    @Override
    public void registerCustomEvent(EventDispatcher eventDispatcher,
            ClientConfigCache clientConfig, ClientBootstrap bootstrap) {
        {
            HashUpdateEventHandler hashUpdateEventHandler = new HashUpdateEventHandler(clientConfig);
            ServerUpdateEventHandler serverUpdateEventHandler = new TestServerUpdateEventHandler(
                    clientConfig);
            UpdateCompletedEventHandler updateCompletedEventHandler = new TestUpdateCompletedEventHandler();
            ServerConnectionEventHandler serverConnectionEventHandler = new ServerConnectionEventHandler(
                    bootstrap, eventDispatcher);

            eventDispatcher.registerEvent(hashUpdateEventHandler, EventType.CLIENT_HASH_UPDATED);

            eventDispatcher
                    .registerEvent(serverUpdateEventHandler, EventType.CLIENT_SERVER_UPDATED);
            eventDispatcher.registerEvent(updateCompletedEventHandler, //
                    EventType.CLIENT_SERVER_UPDATE_COMPLETE, //
                    EventType.CLIENT_HASH_UPDATE_COMPLETE);

            eventDispatcher.registerEvent(serverConnectionEventHandler, //
                    EventType.CLIENT_SERVER_CONNECTED, //
                    EventType.CLIENT_SERVER_INFO_ACCEPTED, //
                    EventType.CLIENT_SERVER_DISCONNECTED, //
                    EventType.CLIENT_SERVER_HEARTBEAT, //
                    EventType.CLIENT_SERVER_HEARTBEAT_TIMOUT);
        }
    }

    public static void main(String[] args) throws Exception {
        int clientType = CLIENT_TYPE_XMPP_SERVER;
        if (args.length > 0) {
            clientType = Integer.valueOf(args[0]);
        }

        new TestSyncClient(clientType).start();
    }
}
