package com.xtra.core.service;

import com.xtra.core.model.LineActivity;
import com.xtra.core.model.LineStatus;
import com.xtra.core.projection.LineAuth;
import com.xtra.core.repository.LineActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LineService {
    private final LineActivityRepository lineActivityRepository;
    private final MainServerApiService mainServerApiService;

    @Autowired
    public LineService(LineActivityRepository lineActivityRepository, MainServerApiService mainServerApiService) {
        this.lineActivityRepository = lineActivityRepository;
        this.mainServerApiService = mainServerApiService;
    }

    public LineStatus authorizeLineForStream(LineAuth lineAuth) {
        return mainServerApiService.sendPostRequest("/lines/stream_auth", LineStatus.class, lineAuth);
    }

    public Long getLineId(String lineToken) {
        return mainServerApiService.sendGetRequest("/lines/get_id/" + lineToken, Long.class);
    }

    public LineStatus authorizeLineForVod(LineAuth lineAuth) {
        return mainServerApiService.sendPostRequest("/lines/vod_auth/", LineStatus.class, lineAuth);

    }

    public boolean killAllConnections(Long lineId) {
        List<LineActivity> lineActivities = lineActivityRepository.findAllByIdLineId(lineId);
        if (!lineActivities.isEmpty()) {
            lineActivities.forEach((activity) -> {
                activity.setHlsEnded(true);
                activity.setEndDate(LocalDateTime.now());
                lineActivityRepository.save(activity);
            });
            return true;
        } else return false;
    }
}
