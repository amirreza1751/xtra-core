package com.xtra.core.projection;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.model.StreamStatus;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
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
    private StreamStatus streamStatus;

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
