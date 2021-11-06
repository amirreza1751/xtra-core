package com.xtra.core.repository;

import com.xtra.core.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByToken(String token);
}
