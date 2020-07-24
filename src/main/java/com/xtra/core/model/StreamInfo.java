package com.xtra.core.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class StreamInfo {
    @Id
    private Long streamId;
    private String uptime;
    private String currentInput;
    private String resolution;
    private String videoCodec;
    private String audioCodec;

    public StreamInfo() {
    }

    public StreamInfo(Long streamId) {
        this.streamId = streamId;
    }

}
