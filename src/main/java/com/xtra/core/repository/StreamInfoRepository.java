package com.xtra.core.repository;

import com.xtra.core.model.StreamInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface StreamInfoRepository extends JpaRepository<StreamInfo, Long> {
    Optional<StreamInfo> findByStreamId(Long streamId);
}
