package com.xtra.core.repository;

import com.xtra.core.model.Process;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.StreamInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface ProgressInfoRepository extends JpaRepository<ProgressInfo, Long> {
    Optional<ProgressInfo> findByStreamId(Long streamId);
}
