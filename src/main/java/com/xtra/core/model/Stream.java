package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Stream {

    private Long id;
    private String name;
    private StreamType streamType;
    private boolean readNative = false;
    private boolean streamAll = false;
    private boolean directSource = false;
    private boolean genTimestamps = false;
    private boolean rtmpOutput = false;
    private String userAgent;

    private TranscodeProfile transcodeProfile;
    private String customFFMPEG;

    private Set<StreamServer> streamServers;

    private List<StreamInput> streamInputs;

    private StreamInput currentInput;
}
