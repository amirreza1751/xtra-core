package com.xtra.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessOutput {
    private String output;
    private int exitValue;

    public ProcessOutput(String output, int exitValue) {
        this.output = output;
        this.exitValue = exitValue;
    }
    public ProcessOutput() {}
}
