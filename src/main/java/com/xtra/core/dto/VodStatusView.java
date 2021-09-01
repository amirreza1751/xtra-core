package com.xtra.core.dto;

import com.xtra.core.model.EncodeStatus;
import lombok.Data;

@Data
public class VodStatusView {
    private EncodeStatus status;
    private String location;
}
