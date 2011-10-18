package com.netease.xmpp.hash.server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.netease.xmpp.hash.HashAlgorithm;

/**
 * Hash algorithm implementation.
 * 
 * @author jiaozhihui@corp.netease.com
 */
public class HashAlgorithmImpl implements HashAlgorithm {
    /*
     * (non-Javadoccc)
     * 
     * @see com.netease.xmpp.hash.HashAlgorithm#hash(byte[], int)
     */
    public long hash(byte[] digest, int nTime) {
        long rv = ((long) (digest[3 + nTime * 4] & 0xFF) << 24)
                | ((long) (digest[2 + nTime * 4] & 0xFF) << 16)
                | ((long) (digest[1 + nTime * 4] & 0xFF) << 8) | (digest[0 + nTime * 4] & 0xFF);

        return rv & 0xffffffffL; /* Truncate to 32-bits */
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.netease.xmpp.hash.HashAlgorithm#hash(java.lang.String)
     */
    public long hash(String k) {
        byte[] digest = computeMd5(k);

        return hash(digest, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.netease.xmpp.hash.HashAlgorithm#computeMd5(java.lang.String)
     */
    public byte[] computeMd5(String k) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes = null;
        try {
            keyBytes = k.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + k, e);
        }

        md5.update(keyBytes);
        return md5.digest();
    }
}
