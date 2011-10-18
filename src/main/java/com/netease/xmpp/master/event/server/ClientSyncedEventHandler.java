package com.netease.xmpp.master.event.server;

import java.io.IOException;

import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.event.UnrecognizedEvent;
import com.netease.xmpp.master.server.ClientCache;
import com.netease.xmpp.master.server.ClientNotifier;

public class ClientSyncedEventHandler implements EventHandler {
    private ClientCache clientCache = null;

    public ClientSyncedEventHandler(ClientCache clientCache) {
        this.clientCache = clientCache;
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        EventType event = ctx.getEvent();
        switch (event) {
        case SERVER_SYNCED:
            checkServerSync();
            break;
        case HASH_SYNCED:
            checkHashSync();
            break;
        default:
            throw new UnrecognizedEvent(event.toString());
        }
    }

    private void checkServerSync() {
        int proxyNum = clientCache.getProxyList().size();
        int robotNum = clientCache.getRobotList().size();

        if (clientCache.getAllProxyServerSync() == proxyNum
                && clientCache.getAllRobotServerSync() == robotNum) {
            ClientNotifier.notifyAllServerSyncCompleted();
        }
    }

    private void checkHashSync() {
        int proxyNum = clientCache.getProxyList().size();
        int robotNum = clientCache.getRobotList().size();

        if (clientCache.getAllProxyHashSync() == proxyNum
                && clientCache.getAllRobotHashSync() == robotNum) {
            ClientNotifier.notifyAllHashSyncCompleted();
        }
    }
}
