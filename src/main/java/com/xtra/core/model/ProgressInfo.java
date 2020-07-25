package com.xtra.core.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
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
