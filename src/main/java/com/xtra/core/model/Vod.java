package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.xtra.core.dto.Resolution;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Vod {
    private Long id;
    private List<Resolution> targetResolutions;
    private String sourceLocation;
    private List<Subtitle> sourceSubtitles;
    private List<Audio> sourceAudios;
}
