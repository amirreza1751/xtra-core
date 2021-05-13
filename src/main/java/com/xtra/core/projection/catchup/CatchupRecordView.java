package com.xtra.core.projection.catchup;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class CatchupRecordView {
    private String title;
    private ZonedDateTime start;
    private ZonedDateTime stop;
    private String streamInput;
}
