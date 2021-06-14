package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Stream {
    @Id
    private Long id;
    private String streamToken;
    private String streamInput;
    private Long pid;

    @OneToOne
    private AdvancedStreamOptions advancedStreamOptions;

    @OneToOne(cascade = CascadeType.REMOVE)
    private ProgressInfo progressInfo;

    @OneToOne(cascade = CascadeType.REMOVE)
    private StreamInfo streamInfo;
}
