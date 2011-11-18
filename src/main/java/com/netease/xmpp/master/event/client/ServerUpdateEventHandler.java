package com.netease.xmpp.master.event.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.netease.xmpp.master.client.ClientConfigCache;
import com.netease.xmpp.master.client.ClientGlobal;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.common.ServerListProtos.Server;
import com.netease.xmpp.master.common.ServerListProtos.Server.ServerInfo;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;

public abstract class ServerUpdateEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(ServerUpdateEventHandler.class);
    
    protected ClientConfigCache config;

    public ServerUpdateEventHandler(ClientConfigCache config) {
        this.config = config;
    }
    @Override
    public void handle(EventContext ctx) throws IOException {
        logger.debug("Start updating server...");
        
        ClientGlobal.setIsServerUpdated(false);
        ClientGlobal.setIsAllServerUpdated(false);
        
        Message data = (Message) ctx.getData();
        byte[] serverData = data.getData();

        ByteArrayInputStream input = new ByteArrayInputStream(serverData);

        Server.Builder server = Server.newBuilder();
        try {
            server.mergeDelimitedFrom(input);
            List<ServerInfo> serverHashList = server.getServerList();
            
            config.setXmppDomain(server.getDomain());
            serverInfoUpdated(data, serverHashList);
            
            ClientGlobal.setIsServerUpdated(true);

            ctx.getDispatcher().dispatchEvent(ctx.getChannel(), data,
                    EventType.CLIENT_SERVER_UPDATE_COMPLETE);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    public abstract void serverInfoUpdated(Message data, List<ServerInfo> serverHashList);
}
