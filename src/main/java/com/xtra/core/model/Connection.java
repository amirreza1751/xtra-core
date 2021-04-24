package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Table(
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"line_id", "stream_id", "user_ip"})
)
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "line_id")
    private Long lineId;

    @NotNull
    @Column(name = "stream_id")
    private Long streamId;

    @NotNull
    @Column(name = "user_ip")
    private String userIp;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime lastRead;

    private boolean hlsEnded;
    private String userAgent;
    private String isp;
    private String country;
    private String city;

}
