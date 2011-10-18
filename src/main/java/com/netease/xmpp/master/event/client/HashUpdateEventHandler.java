package com.netease.xmpp.master.event.client;

import java.io.IOException;

import com.netease.xmpp.hash.HashAlgorithm;
import com.netease.xmpp.hash.HashAlgorithmLoader;
import com.netease.xmpp.master.client.ClientConfigCache;
import com.netease.xmpp.master.common.Message;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.EventType;

public class HashUpdateEventHandler implements EventHandler {
    private ClientConfigCache config = null;

    public HashUpdateEventHandler(ClientConfigCache config) {
        this.config = config;
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        Message data = (Message) ctx.getData();
        byte[] classData = data.getData();

        ClassLoader loader = new HashAlgorithmLoader(HashUpdateEventHandler.class.getClassLoader(),
                classData, config);
        try {
            HashAlgorithm hash = (HashAlgorithm) loader.loadClass(
                    config.getHashAlgorithmClassName()).newInstance();

            config.setHashAlgorithm(hash);
            
            System.out.println("hash version: " + data.getVersion());

            ctx.getDispatcher().dispatchEvent(ctx.getChannel(), data,
                    EventType.CLIENT_HASH_UPDATE_COMPLETE);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
