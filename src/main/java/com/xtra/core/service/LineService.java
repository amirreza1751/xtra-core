package com.xtra.core.service;

import com.xtra.core.model.LineStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class LineService {
    @Value("${main.apiPath}")
    private String mainApiPath;

    public LineStatus authorizeLineForStream(String lineToken, String streamToken) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.getForObject(mainApiPath + "/lines/stream_auth/" + lineToken + "/" + streamToken, LineStatus.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return LineStatus.ERROR;
        }
    }

    public Long getLineId(String lineToken){
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
}
