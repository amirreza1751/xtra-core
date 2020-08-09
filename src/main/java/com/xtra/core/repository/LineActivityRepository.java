package com.xtra.core.repository;

import com.xtra.core.model.LineActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineActivityRepository extends JpaRepository<LineActivity, Long> {
    Optional<LineActivity> findByLineId(Long lineId);

    Optional<LineActivity> findByLineIdAndUserIp(Long lineId, String userIp);
}
