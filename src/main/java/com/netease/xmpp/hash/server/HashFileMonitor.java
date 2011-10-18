package com.netease.xmpp.hash.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import com.netease.xmpp.master.event.EventDispatcher;
import com.netease.xmpp.master.event.EventType;
import com.netease.xmpp.master.server.ServerConfigCache;
import com.netease.xmpp.util.ResourceUtils;

/**
 * Monitor for hash algorithm implementation file.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public class HashFileMonitor implements Runnable {
    private EventDispatcher dispatcher = null;
    private long updateTime = 0;
    private File hashFile = null;
    private String fileName = null;
    private int interval = 0;

    public HashFileMonitor(EventDispatcher dispatcher, ServerConfigCache config) {
        this.dispatcher = dispatcher;
        this.interval = config.getServerMonitorInterval();
        fileName = config.getHashFilePath();

        try {
            hashFile = new File(ResourceUtils.getResourceURI(fileName));
            byte[] hashData = getClassData();

            updateTime = hashFile.lastModified();

            dispatcher.dispatchEvent(null, hashData, EventType.HASH_UPDATED);
        } catch (IOException e) {
            e.printStackTrace();
            throw new HashFileNotFoundException(e);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new HashFileNotFoundException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(interval * 1000);
            } catch (InterruptedException e) {
                // Do nothing
            }

            if (hashFile.lastModified() > updateTime) {
                byte[] hashData = getClassData();
                dispatcher.dispatchEvent(null, hashData, EventType.HASH_UPDATED);
                updateTime = hashFile.lastModified();
            }
        }
    }

    private byte[] getClassData() {
        try {
            InputStream ins = ResourceUtils.getResourceAsStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesNumRead = 0;
            while ((bytesNumRead = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesNumRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
