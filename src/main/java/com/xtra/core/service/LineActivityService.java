package com.xtra.core.service;

import com.xtra.core.model.LineActivity;
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

    public boolean updateLineActivity(Long lineId, Long streamId, String userIp, String userAgent) {
        Optional<LineActivity> existingActivity = lineActivityRepository.findByLineIdAndUserIp(lineId, userIp);
        LineActivity activity;
        if (existingActivity.isPresent()) {
            if (existingActivity.get().isHlsEnded()) {
                return false;
            }
            activity = existingActivity.get();
        } else {
            activity = new LineActivity();
            activity.setLineId(lineId);
            activity.setUserIp(userIp);
        }

        activity.setStreamId(streamId);
        activity.setUserAgent(userAgent);
        activity.setLastRead(LocalDateTime.now());
        lineActivityRepository.save(activity);
        return true;
    }

}