package com.xtra.core.model;

import lombok.Data;

@Data
public class StreamInput {
    private Long id;
    private String url;
    public StreamInput(){}

    public StreamInput(String url) {
        this.url = url;
    }
}
