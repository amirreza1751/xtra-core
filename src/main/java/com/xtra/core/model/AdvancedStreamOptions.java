package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class AdvancedStreamOptions {
    private Boolean generatePts = false;
    private Boolean nativeFrames = false;
    private Boolean streamAllCodecs = false;
    private Boolean allowRecording = false;
    private Boolean outputRTMP = false;
    private Boolean directSource = false;

    private String customChannelSID;
    private String onDemandProbeSize;
    private String minuteDelay;
    private String userAgent;
    private String httpProxy;
    private String cookie;
    private String headers;
    private String transcodingProfile;
}
