package com.xtra.core.model;

import lombok.Data;

import javax.persistence.ElementCollection;
import java.util.List;


@Data
public class Resource {

    private double cpuMaxFreq;
    @ElementCollection
    private List<Double> cpuCurrentFreq;

    private double memoryTotal;
    private double memoryAvailable;

    private String networkName;
    private Long networkBytesSent;
    private Long networkBytesRecv;
    private Long upTime;

    private int connections; // no need to use in constructor.

    public Resource(double cpuMaxFreq, List<Double> cpuCurrentFreq, double memoryTotal, double memoryAvailable, String networkName, Long networkBytesSent, Long networkBytesRecv, Long upTime) {
        this.cpuMaxFreq = cpuMaxFreq;
        this.cpuCurrentFreq = cpuCurrentFreq;
        this.memoryTotal = memoryTotal;
        this.memoryAvailable = memoryAvailable;
        this.networkName = networkName;
        this.networkBytesSent = networkBytesSent;
        this.networkBytesRecv = networkBytesRecv;
        this.upTime = upTime;
    }

}
