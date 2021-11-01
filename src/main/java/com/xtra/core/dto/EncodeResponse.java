package com.xtra.core.dto;

import com.xtra.core.model.EncodeStatus;
import lombok.Data;

import java.util.List;

@Data
public class EncodeResponse {
    EncodeStatus encodeStatus;
    List<VideoInfoView> targetVideoInfos;
}
