package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Entity
public class AdvancedStreamOptions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
