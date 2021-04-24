package com.xtra.core.repository;

import com.xtra.core.model.Connection;
import com.xtra.core.model.ConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByLineId(Long lineId);

    List<Connection> findAllByLineId(Long lineId);

    Optional<Connection> findByLineIdAndUserIpAndStreamId(Long lineId, String userIp, Long streamId);

    List<Connection> findAllByLastReadIsLessThanEqual(LocalDateTime lastReadBefore);

    List<Connection> findAllByHlsEndedAndEndDateBefore(boolean hlsEnded, LocalDateTime endDateBefore);

    List<Connection> findAllByHlsEnded(boolean hlsEnded);
}
