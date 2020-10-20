package com.xtra.core.model;

import lombok.Data;

import javax.persistence.Column;


@Data
public class NetworkInterface {

    private String name;
    private Long BytesSent;
    private Long BytesRecv;

    public NetworkInterface(String name, Long bytesSent, Long bytesRecv) {
        this.name = name;
        BytesSent = bytesSent;
        BytesRecv = bytesRecv;
    }
    public NetworkInterface(){}

    private Resource resource;

    @Column(name = "resource_id")
    private Long resourceId;
}
