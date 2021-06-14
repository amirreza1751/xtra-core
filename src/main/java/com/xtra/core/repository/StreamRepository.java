package com.xtra.core.repository;

import com.xtra.core.model.Stream;
import com.xtra.core.projection.IdOnly;
import com.xtra.core.projection.PidOnly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, Long> {
    List<PidOnly> findAllBy();

    Optional<IdOnly> findByStreamToken(String streamToken);

    List<Stream> findAllByIdIn(List<Long> ids);
}
