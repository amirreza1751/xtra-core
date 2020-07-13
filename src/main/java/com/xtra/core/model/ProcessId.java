package com.xtra.core.model;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class ProcessId implements Serializable {
    private Long pid, streamId;

    public ProcessId(){}
    public ProcessId(Long streamId, Long pid) {
        this.streamId = streamId;
        this.pid = pid;
    }
}
