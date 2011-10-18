package com.netease.xmpp.hash.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.netty.channel.Channel;

import com.netease.xmpp.hash.HashAlgorithm;
import com.netease.xmpp.master.common.ServerHashProtos.Server;
import com.netease.xmpp.master.common.ServerHashProtos.Server.ServerHash;
import com.netease.xmpp.master.server.ServerConfigCache;

/**
 * Used by xmpp server hash.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public final class KetamaNodeLocator {
    private TreeMap<Long, Channel> ketamaNodes;

    public KetamaNodeLocator(List<Channel> serverNodes, ServerConfigCache config) {
        HashAlgorithm hashAlg = config.getHashAlgorithm();
        int numReps = config.getServerRepNum();

        ketamaNodes = new TreeMap<Long, Channel>();
        List<Channel> serverNodesCopy = new LinkedList<Channel>(serverNodes);

        for (Channel node : serverNodesCopy) {
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

        for (Map.Entry<Long, Channel> entry : ketamaNodes.entrySet()) {
            Long hash = entry.getKey();
            Channel server = entry.getValue();

            InetSocketAddress address = (InetSocketAddress) server.getRemoteAddress();
            
            ServerHash.Builder serverHash = ServerHash.newBuilder();
            serverHash.setHash(hash);
            serverHash.setIp(address.getAddress().getHostAddress());
            serverHash.setPort(address.getPort());

            serverHashList.addServer(serverHash.build());
        }

        serverHashList.build().writeDelimitedTo(output);

        return output.toByteArray();
    }

    private String getServerAddr(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
        StringBuilder sb = new StringBuilder(address.getAddress().getHostAddress());
        sb.append(":").append(address.getPort());

        return sb.toString();
    }
}
