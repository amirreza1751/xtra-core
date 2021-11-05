package com.xtra.core.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EncodeRequest {
    private Long videoId;
    private String sourceLocation;
    private List<AudioDetails> sourceAudios;
    private VideoCodec targetVideoCodec;
    private AudioCodec targetAudioCodec;
    private List<Resolution> targetResolutions;
}