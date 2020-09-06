package com.xtra.core.service;

import com.xtra.core.model.LineActivity;
import com.xtra.core.model.LineStatus;
import com.xtra.core.repository.LineActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LineService {
    @Value("${main.apiPath}")
    private String mainApiPath;
    private final LineActivityRepository lineActivityRepository;
    private final MainServerApiService mainServerApiService;

    @Autowired
    public LineService(LineActivityRepository lineActivityRepository, MainServerApiService mainServerApiService) {
        this.lineActivityRepository = lineActivityRepository;
        this.mainServerApiService = mainServerApiService;
    }

    public LineStatus authorizeLineForStream(String lineToken, String streamToken) {
        try {
            return mainServerApiService.sendGetRequest(mainApiPath + "/lines/stream_auth/" + lineToken + "/" + streamToken, LineStatus.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return LineStatus.ERROR;
        }
    }

    public Long getLineId(String lineToken) {
        try {
            return mainServerApiService.sendGetRequest(mainApiPath + "/lines/get_id/" + lineToken, Long.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }

    public LineStatus authorizeLineForVod(String lineToken, String vodToken) {
        try {
            return mainServerApiService.sendGetRequest(mainApiPath + "/lines/vod_auth/" + lineToken + "/" + vodToken, LineStatus.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return LineStatus.ERROR;
        }
    }

    public boolean killAllConnections(Long lineId) {
        List<LineActivity> lineActivities = lineActivityRepository.findAllByLineId(lineId);
        if (!lineActivities.isEmpty()) {
            lineActivities.forEach((activity)->{
                activity.setHlsEnded(true);
                activity.setEndDate(LocalDateTime.now());
                lineActivityRepository.save(activity);
            });
            return true;
        } else return false;
    }
}
