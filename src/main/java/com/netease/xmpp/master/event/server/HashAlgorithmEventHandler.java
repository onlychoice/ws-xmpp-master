package com.netease.xmpp.master.event.server;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.log4j.Logger;

import com.netease.xmpp.hash.HashAlgorithm;
import com.netease.xmpp.hash.HashAlgorithmLoader;
import com.netease.xmpp.master.event.EventContext;
import com.netease.xmpp.master.event.EventHandler;
import com.netease.xmpp.master.event.client.HashUpdateEventHandler;
import com.netease.xmpp.master.server.ClientCache;
import com.netease.xmpp.master.server.ClientNotifier;
import com.netease.xmpp.master.server.ServerConfigCache;

public class HashAlgorithmEventHandler implements EventHandler {
    private static Logger logger = Logger.getLogger(HashAlgorithmEventHandler.class);

    private ServerConfigCache config = null;
    private ClientCache clientCache = null;

    public HashAlgorithmEventHandler(ServerConfigCache config, ClientCache clientCache) {
        this.config = config;
        this.clientCache = clientCache;
    }

    @Override
    public void handle(EventContext ctx) throws IOException {
        logger.debug("Hash file updated...");

        final byte[] hashData = (byte[]) ctx.getData();

        ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return new HashAlgorithmLoader(HashUpdateEventHandler.class.getClassLoader(),
                        hashData, config);
            }
        });

        try {
            config.setHashAlgorithmCode(hashData);

            HashAlgorithm hash = (HashAlgorithm) loader.loadClass(
                    config.getHashAlgorithmClassName()).newInstance();

            clientCache.incrHashVersion();

            config.setHashAlgorithm(hash);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        ClientNotifier.notifyAllHashUpdate();
    }
}
