package com.xtra.core.dto;

import lombok.Data;

import java.time.Duration;

@Data
public class VideoInfoView {
    private Duration duration;
    private Resolution resolution;
    private VideoCodec videoCodec;
    private AudioCodec audioCodec;
    private String fileSize;
}
