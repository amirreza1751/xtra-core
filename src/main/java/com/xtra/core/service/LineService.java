package com.xtra.core.service;

import com.xtra.core.model.LineActivity;
import com.xtra.core.model.LineStatus;
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

    public LineStatus authorizeLineForStream(String lineToken, String streamToken) {
        return mainServerApiService.sendGetRequest("/lines/stream_auth/" + lineToken + "/" + streamToken, LineStatus.class);
    }

    public Long getLineId(String lineToken) {
        return mainServerApiService.sendGetRequest("/lines/get_id/" + lineToken, Long.class);
    }

    public LineStatus authorizeLineForVod(String lineToken, String vodToken) {
        return mainServerApiService.sendGetRequest("/lines/vod_auth/" + lineToken + "/" + vodToken, LineStatus.class);

    }

    public boolean killAllConnections(Long lineId) {
        List<LineActivity> lineActivities = lineActivityRepository.findAllByLineId(lineId);
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
