package com.xtra.core.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
public class Process {
    @EmbeddedId
    private ProcessId processId;
    public Process(){}

}

@Embeddable
class ProcessId implements Serializable{
    private Long pid, streamId;
}

