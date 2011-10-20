package com.netease.xmpp.master.event;

public enum EventType {
    // ==================XMPP SERVER=====================//
    /**
     * XMPP server connected
     */
    SERVER_CONNECT,
    /**
     * XMPP server info received
     */
    SERVER_INFO_RECV,
    /**
     * XMPP server disconnected
     */
    SERVER_DISCONNECT,
    /**
     * XMPP server exception
     */
    SERVER_EXCEPTION,
    /**
     * XMPP server heart beat
     */
    SERVER_HEARTBEAT,
    /**
     * Start heart beat.
     */
    SERVER_HEARTBEAT_START,
    /**
     * Stop heart beat.
     */
    SERVER_HEARTBEAT_STOP,
    /**
     * Heart beat timeout.
     */
    SERVER_HEARTBEAT_TIMEOUT,

    // ==================PROXY=====================//
    /**
     * Proxy connected
     */
    PROXY_CONNECT,
    /**
     * Proxy disconnected
     */
    PROXY_DISCONNECT,
    /**
     * Proxy exception
     */
    PROXY_EXCEPTION,
    /**
     * Proxy heart beat
     */
    PROXY_HEARTBEAT,
    /**
     * Start heart beat.
     */
    PROXY_HEARTBEAT_START,
    /**
     * Stop heart beat.
     */
    PROXY_HEARTBEAT_STOP,
    /**
     * Heart beat timeout.
     */
    PROXY_HEARTBEAT_TIMEOUT,
    /**
     * Proxy update server info successfully.
     */
    PROXY_SERVER_UPDATE_COMPLETE,
    /**
     * Proxy update hash successfully.
     */
    PROXY_HASH_UPDATE_COMPLETE,

    // ==================ROBOT=====================//
    /**
     * Robot connected
     */
    ROBOT_CONNECT,
    /**
     * Robot disconnected
     */
    ROBOT_DISCONNECT,
    /**
     * Robot exception
     */
    ROBOT_EXCEPTION,
    /**
     * Robot heart beat
     */
    ROBOT_HEARTBEAT,
    /**
     * Start heart beat.
     */
    ROBOT_HEARTBEAT_START,
    /**
     * Stop heart beat.
     */
    ROBOT_HEARTBEAT_STOP,
    /**
     * Heart beat timeout.
     */
    ROBOT_HEARTBEAT_TIMEOUT,
    /**
     * Robot update server info successfully.
     */
    ROBOT_SERVER_UPDATE_COMPLETE,
    /**
     * Robot update hash successfully.
     */
    ROBOT_HASH_UPDATE_COMPLETE,

    // ==================HASH=====================//
    /**
     * Hash algorithm updated, for server event.
     */
    HASH_UPDATED,

    // ==================CLIENT=====================//
    /**
     * Indicate that sever info have been synced on on client
     */
    SERVER_SYNCED,
    /**
     * Indicate that hash info have been synced on on client
     */
    HASH_SYNCED,

    /**
     * Same to HASH_UPDATE, but for client event.
     */
    CLIENT_HASH_UPDATED,

    /**
     * Server list updated.
     */
    CLIENT_SERVER_UPDATED,

    /**
     * Server info update successfully, for client event.
     */
    CLIENT_SERVER_UPDATE_COMPLETE,
    /**
     * Hash info update successfully, for client event.
     */
    CLIENT_HASH_UPDATE_COMPLETE,

    /**
     * Client disconnected from master server.
     */
    CLIENT_SERVER_DISCONNECTED,
    /**
     * Client connected with master server.
     */
    CLIENT_SERVER_CONNECTED,

    /**
     * Heart beat from master server.
     */
    CLIENT_SERVER_HEARTBEAT,
    /**
     * Heart beat timeout.
     */
    CLIENT_SERVER_HEARTBEAT_TIMOUT,
    
    /**
     * Server info accepted.
     */
    CLIENT_SERVER_INFO_ACCEPTED
}
