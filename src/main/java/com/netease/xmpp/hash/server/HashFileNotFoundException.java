package com.netease.xmpp.hash.server;

public class HashFileNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -121507066543318223L;

    public HashFileNotFoundException(String message) {
        super(message);
    }

    public HashFileNotFoundException(Throwable cause) {
        super(cause);
    }
}
