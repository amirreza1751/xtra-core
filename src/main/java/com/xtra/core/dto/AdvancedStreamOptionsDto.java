package com.xtra.core.dto;

import lombok.Data;

@Data
public class AdvancedStreamOptionsDto {
    private boolean generatePts;
    private boolean nativeFrames;
    private boolean streamAllCodecs;
    private boolean allowRecording;
    private boolean outputRTMP;
    private boolean directSource;

    private String customChannelSID;
    private int probeSize;
    private int minuteDelay;
    private String userAgent;
    private String httpProxy;
    private String cookie;
    private String headers;
    private String transcodingProfile;
}
