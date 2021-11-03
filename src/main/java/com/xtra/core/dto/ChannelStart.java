package com.xtra.core.dto;

import lombok.Data;

@Data
public class ChannelStart {
    private Long id;
    private String name;
    private String streamToken;
    private String streamInput;

    //advanced stream options
    private AdvancedStreamOptionsDto advancedStreamOptions;
}
