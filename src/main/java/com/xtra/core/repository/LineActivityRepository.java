package com.xtra.core.repository;

import com.xtra.core.model.LineActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LineActivityRepository extends JpaRepository<LineActivity, Long> {
    Optional<LineActivity> findByLineId(Long lineId);

    List<LineActivity> findAllByLineId(Long lineId);

    Optional<LineActivity> findByLineIdAndUserIp(Long lineId, String userIp);

    List<LineActivity> findAllByLastReadIsLessThanEqual(LocalDateTime lastReadBefore);

    List<LineActivity> findAllByHlsEndedAndEndDateBefore(boolean hlsEnded, LocalDateTime endDateBefore);

    List<LineActivity> findAllByHlsEnded(boolean hlsEnded);
}
