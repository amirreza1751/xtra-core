package com.xtra.core.repository;

import com.xtra.core.model.LineActivity;
import com.xtra.core.model.LineActivityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LineActivityRepository extends JpaRepository<LineActivity, LineActivityId> {
    Optional<LineActivity> findByIdLineId(Long lineId);

    List<LineActivity> findAllByIdLineId(Long lineId);

    Optional<LineActivity> findByIdLineIdAndIdUserIpAndIdStreamId(Long lineId, String userIp, Long streamId);

    List<LineActivity> findAllByLastReadIsLessThanEqual(LocalDateTime lastReadBefore);

    List<LineActivity> findAllByHlsEndedAndEndDateBefore(boolean hlsEnded, LocalDateTime endDateBefore);

    List<LineActivity> findAllByHlsEnded(boolean hlsEnded);

    void deleteById(LineActivityId lineActivityId);
}
