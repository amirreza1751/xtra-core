package com.xtra.core.model;

import lombok.Data;

@Data
public class StreamInfo {
    private Long streamId;
    private String uptime;
    private String currentInput;
    private String bitrate;
    private String resolution;
    private String videoCodec;
    private String audioCodec;
    private String speed;
    private String frameRate;

    public StreamInfo(Long streamId, String uptime, String currentInput, String bitrate, String resolution, String videoCodec, String audioCodec, String speed, String frameRate) {
        this.streamId = streamId;
        this.uptime = uptime;
        this.currentInput = currentInput;
        this.bitrate = bitrate;
        this.resolution = resolution;
        this.videoCodec = videoCodec;
        this.audioCodec = audioCodec;
        this.speed = speed;
        this.frameRate = frameRate;
    }
}
