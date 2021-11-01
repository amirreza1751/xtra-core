package com.xtra.core.dto;

import lombok.Data;

import java.util.List;

@Data
public class EncodeRequest {
    private Long videoId;
    private String sourceLocation;
    private List<AudioDetails> sourceAudios;
    private VideoCodec targetVideoCodec;
    private AudioCodec targetAudioCodec;
    private List<Resolution> targetResolutions;
}