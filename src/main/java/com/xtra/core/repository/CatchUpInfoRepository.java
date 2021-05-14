package com.xtra.core.repository;

import com.xtra.core.model.CatchUpInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatchUpInfoRepository extends JpaRepository<CatchUpInfo, Long> {
    Optional<CatchUpInfo> findByStreamId(Long streamId);
}
