package com.xtra.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Data
public class Line extends User {
    private long id;
    private LocalDateTime expireDate;
    private int maxConnections = 1;
    private boolean isReStreamer = false;
    private boolean isTrial;
    private boolean isBlocked = false;
    private boolean isAdminBlocked = false;
    private boolean isIspLocked = false;
    private boolean isStalker;
    private String notes;

    private User referrer;
    private User reseller;

}
