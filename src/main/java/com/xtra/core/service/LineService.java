package com.xtra.core.service;

import com.xtra.core.model.LineActivity;
import com.xtra.core.model.LineStatus;
import com.xtra.core.repository.LineActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

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
            return restTemplate.getForObject(mainApiPath + "/lines/stream_auth/" + lineToken + "/" + streamToken, LineStatus.class);
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
