package com.netease.xmpp.hash.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.netease.xmpp.hash.HashAlgorithm;
import com.netease.xmpp.master.common.ServerListProtos.Server;
import com.netease.xmpp.master.common.ServerListProtos.Server.ServerInfo;
import com.netease.xmpp.master.server.ServerConfigCache;

/**
 * Used by xmpp server hash.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public final class KetamaNodeLocator {
    private TreeMap<Long, ServerInfo> ketamaNodes;

    public KetamaNodeLocator(Collection<ServerInfo> serverNodes, ServerConfigCache config) {
        HashAlgorithm hashAlg = config.getHashAlgorithm();
        int numReps = config.getServerRepNum();

        ketamaNodes = new TreeMap<Long, ServerInfo>();

        for (ServerInfo node : serverNodes) {
            for (int i = 0; i < numReps / 4; i++) {
                byte[] digest = hashAlg.computeMd5(getServerAddr(node) + i);
                for (int h = 0; h < 4; h++) {
                    Long k = hashAlg.hash(digest, h);
                    ketamaNodes.put(k, node);
                }

            }
        }
    }

    public byte[] getServerHashList() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Server.Builder serverHashList = Server.newBuilder();

        for (Map.Entry<Long, ServerInfo> entry : ketamaNodes.entrySet()) {
            Long hash = entry.getKey();
            ServerInfo server = entry.getValue();

            ServerInfo.Builder serverInfoBuilder = ServerInfo.newBuilder();
            serverInfoBuilder.mergeFrom(server);
            serverInfoBuilder.setHash(hash);

            serverHashList.addServer(serverInfoBuilder.build());
        }

        serverHashList.build().writeDelimitedTo(output);

        return output.toByteArray();
    }

    private String getServerAddr(ServerInfo serverInfo) {
        StringBuilder sb = new StringBuilder(serverInfo.getIp());
        sb.append(":").append(serverInfo.getCMPort());

        return sb.toString();
    }
}
