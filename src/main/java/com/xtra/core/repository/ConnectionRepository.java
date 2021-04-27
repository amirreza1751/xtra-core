package com.xtra.core.repository;

import com.xtra.core.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByLineToken(String lineToken);

    List<Connection> findAllByLineToken(String lineToken);

    Optional<Connection> findByLineTokenAndStreamTokenAndUserIp(String lineToken, String streamId, String userIp);

    List<Connection> findAllByLastReadIsLessThanEqual(LocalDateTime lastReadBefore);

    List<Connection> findAllByHlsEndedAndEndDateBefore(boolean hlsEnded, LocalDateTime endDateBefore);

    List<Connection> findAllByHlsEnded(boolean hlsEnded);
}
