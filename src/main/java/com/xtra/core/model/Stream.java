package com.xtra.core.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import java.util.List;
@Data
public class Stream {

    private Long id;
    private String name;
    private StreamType streamType;
    private boolean readNative = false;
    private boolean streamAll = false;
    private boolean directSource = false;
    private boolean genTimestamps = false;
    private boolean rtmpOutput = false;

    private TranscodeProfile transcodeProfile;
    private String customFFMPEG;

    private List<Server> servers;

    private List<StreamInput> streamInputs;

    private StreamInput currentInput;
}
