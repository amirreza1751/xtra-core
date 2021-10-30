package com.xtra.core.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.model.StreamStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private LocalDateTime lastUpdated;

    public StreamDetailsView(Long streamId) {
        this.streamId = streamId;
    }

    public StreamDetailsView(Long streamId, String uptime, String currentInput, String resolution, String videoCodec, String audioCodec, String speed, String frameRate, String bitrate) {
        this.streamId = streamId;
        this.uptime = uptime;
        this.currentInput = currentInput;
        this.resolution = resolution;
        this.videoCodec = videoCodec;
        this.audioCodec = audioCodec;
        this.speed = speed;
        this.frameRate = frameRate;
        this.bitrate = bitrate;
    }
}
