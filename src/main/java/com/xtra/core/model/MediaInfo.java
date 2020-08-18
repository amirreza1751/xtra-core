package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.Duration;
import java.time.Period;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MediaInfo {
    private String resolution;
    private String videoCodec;
    private String audioCodec;
    private Duration duration;
}
