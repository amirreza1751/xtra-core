package com.xtra.core.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
public class Process {
    @EmbeddedId
    private ProcessId processId;
    public Process() {
    }
    public Process(Long streamId, Long pid) {
        this.processId = new ProcessId(streamId, pid);
    }

    public Long getPid(){
        return this.processId.getPid();
    }

    public Long getStreamId(){
        return this.processId.getStreamId();
    }
}

