package com.xtra.core.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.model.StreamStatus;
import lombok.Data;

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

}
