package com.xtra.core.repository;

import com.xtra.core.model.ProgressInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressInfoRepository extends JpaRepository<ProgressInfo, Long> {
    Optional<ProgressInfo> findByStreamId(Long streamId);
}
