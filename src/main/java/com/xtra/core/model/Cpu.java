package com.xtra.core.model;

import lombok.Data;


@Data
public class Cpu {

    private String description;
    private double maxFreq;
    private double[] currentFreq;

    public Cpu(String description, double maxFreq, double[] currentFreq){
        this.description = description;
        this.maxFreq = maxFreq;
        this.currentFreq = currentFreq;
    }

}
