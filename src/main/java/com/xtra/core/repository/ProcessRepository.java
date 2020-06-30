package com.xtra.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.xtra.core.model.Process;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    Optional<Process> findByStreamId(Long streamId);
}
