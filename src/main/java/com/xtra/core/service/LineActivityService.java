package com.xtra.core.service;

import com.xtra.core.model.Connection;
import com.xtra.core.model.ConnectionId;
import com.xtra.core.repository.LineActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LineActivityService {
    private final LineActivityRepository lineActivityRepository;

    @Autowired
    public LineActivityService(LineActivityRepository lineActivityRepository) {
        this.lineActivityRepository = lineActivityRepository;
    }

    public boolean updateLineActivity(ConnectionId connectionId, String userAgent) {
        Optional<Connection> existingActivity = lineActivityRepository.findByIdLineIdAndIdUserIpAndIdStreamId(connectionId.getLineId(), connectionId.getUserIp(), connectionId.getStreamId());
        Connection activity;
        if (existingActivity.isPresent()) {
            if (existingActivity.get().isHlsEnded()) {
                return false;
            }
            activity = existingActivity.get();
        } else {
            activity = new Connection();
        }
        activity.setId(connectionId);

        activity.setUserAgent(userAgent);
        activity.setLastRead(LocalDateTime.now());
        lineActivityRepository.save(activity);
        return true;
    }

}
