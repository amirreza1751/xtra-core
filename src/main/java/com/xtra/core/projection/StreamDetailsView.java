package com.xtra.core.projection;

import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.StreamInfo;
import lombok.Data;

import java.io.Serializable;

@Data
public class StreamDetailsView {
    private Long streamId;
    private String uptime;
    private String currentInput;
    private String resolution;
    private String videoCodec;
    private String audioCodec;
    private String speed;
    private String frameRate;
    private String bitrate;

    public void updateStreamInfo(StreamInfo streamInfo){
        streamId = streamInfo.getStreamId();
        uptime = streamInfo.getUptime();
        audioCodec = streamInfo.getAudioCodec();
        videoCodec = streamInfo.getVideoCodec();
        currentInput = streamInfo.getCurrentInput();
        resolution = streamInfo.getResolution();
    }

    public void updateProgressInfo(ProgressInfo progressInfo){
        speed = progressInfo.getSpeed();
        frameRate = progressInfo.getFrameRate();
        bitrate = progressInfo.getBitrate();
    }
}