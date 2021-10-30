package com.xtra.core.repository;

import com.xtra.core.dto.StreamDetailsView;
import com.xtra.core.model.Stream;
import com.xtra.core.projection.IdOnly;
import com.xtra.core.projection.PidOnly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, Long> {
    List<PidOnly> findAllBy();

    Optional<IdOnly> findByStreamToken(String streamToken);

    List<Stream> findAllByIdIn(List<Long> ids);

    /*@Query(value = "SELECT\n" +
            "    s.id AS id,\n" +
            "    si.audio_codec as audioCodec,\n" +
            "    si.current_input as currentInput,\n" +
            "    si.resolution,\n" +
            "    si.uptime,\n" +
            "    si.video_codec as videoCodec,\n" +
            "    PI.bitrate,\n" +
            "    PI.frame_rate as frameRate,\n" +
            "    PI.speed\n" +
            "FROM\n" +
            "    stream s\n" +
            "LEFT JOIN stream_info si ON\n" +
            "    s.stream_info_id = si.id\n" +
            "LEFT JOIN progress_info PI ON\n" +
            "    s.progress_info_id = PI.id", nativeQuery = true)*/
    @Query(value = "select new com.xtra.core.dto.StreamDetailsView(s.id, si.uptime, si.currentInput, si.resolution, si.videoCodec, si.audioCodec" +
            ",pi.speed, pi.frameRate, pi.bitrate) from Stream s left join s.streamInfo si left join s.progressInfo pi")
    List<StreamDetailsView> findAllStreamDetails();
}
