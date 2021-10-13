package com.xtra.core.model;

import lombok.Data;

import javax.persistence.ElementCollection;
import java.util.List;


@Data
public class Resource {

    private double cpuMaxFreq;
    private double cpuLoad;
    private double memoryTotal;
    private double memoryAvailable;

    private String networkName;
    private Long networkBytesSent;
    private Long networkBytesRecv;

    private Long upTime;

    private int connections; // no need to use in constructor.
    public Resource(double cpuMaxFreq, double cpuLoad, double memoryTotal, double memoryAvailable, String networkName, Long networkBytesSent, Long networkBytesRecv, Long upTime) {
        this.cpuMaxFreq = cpuMaxFreq;
        this.cpuLoad = cpuLoad;
        this.memoryTotal = memoryTotal;
        this.memoryAvailable = memoryAvailable;
        this.networkName = networkName;
        this.networkBytesSent = networkBytesSent;
        this.networkBytesRecv = networkBytesRecv;
        this.upTime = upTime;
    }
    public Resource (){}

}
