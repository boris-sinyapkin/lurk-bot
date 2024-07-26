package com.lurk.statistics;

import java.net.URI;

public class LurkNode {

    private final String ip;
    private final short port;

    public LurkNode(String ip, short port) {
        this.ip = ip;
        this.port = port;
    }

    public URI getHttpUri(String path) {
        return URI.create(String.format("http://%s:%d%s", ip, port, path));
    }

    @Override
    public String toString() {
        return String.format("%s:%d", ip, port);
    }
}
