package com.xtra.core.projection;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LineAuth {
    private String lineToken;
    private String mediaToken;
    private String ipAddress;
    private String userAgent;

    public LineAuth(String lineToken, String mediaToken, String ipAddress, String userAgent) {
        this.lineToken = lineToken;
        this.mediaToken = mediaToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

}
