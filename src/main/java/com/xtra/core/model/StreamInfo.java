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
    private String bitrate;
    private String resolution;
    private String videoCodec;
    private String audioCodec;
    private String speed;
    private String frameRate;

    public StreamInfo(Long streamId){
        this.streamId = streamId;
    }

    public StreamInfo(){

    }
}
