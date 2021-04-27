package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Table(
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"line_token", "stream_token", "user_ip"})
)
@NoArgsConstructor
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "line_token")
    private String lineToken;

    @NotNull
    @Column(name = "stream_token")
    private String streamToken;

    @NotNull
    @Column(name = "user_ip")
    private String userIp;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime lastRead;

    private boolean hlsEnded;
    private String userAgent;

    public Connection(String lineToken, String streamToken, String userIp) {
        this.lineToken = lineToken;
        this.streamToken = streamToken;
        this.userIp = userIp;
        this.startDate = LocalDateTime.now();
    }
}
