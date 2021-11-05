package com.xtra.core.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.xtra.core.model.EncodeStatus;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EncodeResponse {
    EncodeStatus encodeStatus;
    List<VideoInfoView> targetVideoInfos;
}
