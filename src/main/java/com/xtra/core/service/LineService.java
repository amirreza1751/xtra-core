package com.xtra.core.service;

import com.xtra.core.model.LineStatus;
import com.xtra.core.repository.LineActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class LineService {
    @Value("${main.apiPath}")
    private String mainApiPath;
    private final LineActivityRepository lineActivityRepository;

    @Autowired
    public LineService(LineActivityRepository lineActivityRepository) {
        this.lineActivityRepository = lineActivityRepository;
    }

    public LineStatus authorizeLineForStream(String lineToken, String streamToken) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.getForObject(mainApiPath + "/lines/stream_auth/" + lineToken + "/" + streamToken, LineStatus.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return LineStatus.ERROR;
        }
    }

    public Long getLineId(String lineToken) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.getForObject(mainApiPath + "/lines/get_id/" + lineToken, Long.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }

    public LineStatus authorizeLineForVod(String lineToken, String streamToken) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.getForObject(mainApiPath + "/lines/vod_auth/" + lineToken + "/" + streamToken, LineStatus.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return LineStatus.ERROR;
        }
    }

    public boolean killConnection(Long lineId) {
        var activityById = lineActivityRepository.findByLineId(lineId);
        if (activityById.isPresent()) {
            var activity = activityById.get();
            activity.setHlsEnded(true);
            activity.setEndDate(LocalDateTime.now());
            lineActivityRepository.save(activity);
            return true;
        }
        else return false;
    }
}
