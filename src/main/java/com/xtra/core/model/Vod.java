package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Vod {
    private Long id;

    private String name;
    private String location;

    private EncodingStatus encodeStatus;

    private List<Subtitle> subtitles = new ArrayList<>();

    private List<Audio> audios = new ArrayList<>();
}
