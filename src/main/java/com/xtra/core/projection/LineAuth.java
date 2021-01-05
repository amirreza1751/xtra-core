package com.xtra.core.projection;

import lombok.Data;

import java.net.Inet4Address;

@Data
public class LineAuth {
    private String lineToken;
    private String mediaToken;
    private String ipAddress;
    private String userAgent;

    public LineAuth(String lineToken, String mediaToken, String ipAddress, String userAgent) {
        this.lineToken = lineToken;
        this.mediaToken = mediaToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public LineAuth() {
    }
}
