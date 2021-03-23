package com.xtra.core.model;

import lombok.Data;

import java.util.List;

@Data
public class Stream {

    private Long id;
    private String name;
    private StreamType streamType;
    private ProgressInfo progressInfo;
    private int selectedSource;
    private boolean readNative = false;
    private boolean streamAll = false;
    private boolean directSource = false;
    private boolean genTimestamps = false;
    private boolean rtmpOutput = false;
    private String userAgent;

    private TranscodeProfile transcodeProfile;
    private String customFFMPEG;

    private List<StreamInput> streamInputs;

    private StreamInput currentInput;
}
