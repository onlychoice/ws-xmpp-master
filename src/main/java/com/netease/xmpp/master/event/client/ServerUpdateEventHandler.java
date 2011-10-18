package com.netease.xmpp.master.event.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.common.ServerHashProtos.Server;
import com.netease.xmpp.master.common.ServerHashProtos.Server.ServerHash;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;

public class ServerUpdateEventHandler implements EventHandler {

    @Override
    public void handle(EventContext ctx) throws IOException {
        Message data = (Message) ctx.getData();
        byte[] serverData = data.getData();

        ByteArrayInputStream input = new ByteArrayInputStream(serverData);

        Server.Builder server = Server.newBuilder();
        try {
            server.mergeDelimitedFrom(input);

            List<ServerHash> serverHashList = server.getServerList();
            for (ServerHash sh : serverHashList) {
                System.out.println("IP: " + sh.getIp() + ", PORT: " + sh.getPort() + ", HASH: "
                        + sh.getHash());
            }
            // TODO do the actual server update work
            System.out.println("server version: " + data.getVersion());

            ctx.getDispatcher().dispatchEvent(ctx.getChannel(), data,
                    EventType.CLIENT_SERVER_UPDATE_COMPLETE);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
