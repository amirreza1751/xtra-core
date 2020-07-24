package com.xtra.core.repository;

import com.xtra.core.model.StreamInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreamInfoRepository extends JpaRepository<StreamInfo, Long> {
    StreamInfo findByStreamId(Long streamId);
}
