package com.xtra.core.model;

import lombok.Data;

import javax.persistence.ElementCollection;
import java.util.List;


@Data
public class Resource {

    private double cpuMaxFreq;
    @ElementCollection
    private List<Float> cpuCurrentFreq;

    private double memoryTotal;
    private double memoryAvailable;

    private String networkName;
    private Long networkBytesSent;
    private Long networkBytesRecv;

    private int connections; // no need to use in constructor.

    public Resource(double cpuMaxFreq, List<Float> cpuCurrentFreq, double memoryTotal, double memoryAvailable, String networkName, Long networkBytesSent, Long networkBytesRecv) {
        this.cpuMaxFreq = cpuMaxFreq;
        this.cpuCurrentFreq = cpuCurrentFreq;
        this.memoryTotal = memoryTotal;
        this.memoryAvailable = memoryAvailable;
        this.networkName = networkName;
        this.networkBytesSent = networkBytesSent;
        this.networkBytesRecv = networkBytesRecv;
        this.upTime = upTime;
    }
    public Resource (){}

}
