package com.xtra.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.xtra.core.model.Process;

import javax.transaction.Transactional;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    Optional<Process> findByProcessIdStreamId(Long streamId);

    @Transactional
    Long deleteByProcessIdStreamId(Long streamId);

    Optional<Process> findByProcessIdPid(Long pid);

    List<Process> findByProcessIdStreamIdIn(List<Long> streamIds);
}
