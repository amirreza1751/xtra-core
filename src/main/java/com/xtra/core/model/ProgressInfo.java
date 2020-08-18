package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProgressInfo {
    @Id
    private Long streamId;
    private String speed;
    private String frameRate;
    private String bitrate;

    public ProgressInfo() {

    }

    public ProgressInfo(Long streamId) {
        this.streamId = streamId;
    }
}
