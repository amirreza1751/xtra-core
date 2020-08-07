package com.xtra.core.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
public class LineActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long lineId;
    private Long streamId;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean hlsEnded;
    private String userIp;
    private String userAgent;
    private String isp;
    private String country;
    private String city;

}
