package com.xtra.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class MainServerApiService {
    private final RestTemplate restTemplate;
    @Value("${main.apiPath}")
    private String mainApiPath;

    public MainServerApiService() {
        this.restTemplate = new RestTemplate();
    }

    public <T> T sendGetRequest(String path, Class<T> tClass) {
        String uri = mainApiPath + path;
        ResponseEntity<T> result = restTemplate.getForEntity(uri, tClass);
        return result.getBody();
    }

    public <T> T sendPostRequest(String path, Class<T> tClass, Object data) {
        String uri = mainApiPath + path;
        ResponseEntity<T> result = restTemplate.postForEntity(mainApiPath, data, tClass);
        return result.getBody();
    }

    public <T> void sendPatchRequest(String path, Class<T> tClass, Object data) {
        String uri = mainApiPath + path;
        restTemplate.patchForObject(mainApiPath, data, tClass, new HashMap<>());
    }

    public <T> void sendDeleteRequest(String path) {
        String uri = mainApiPath + path;
        restTemplate.delete(mainApiPath);
    }

    public <T> void sendPutRequest(String path, Class<T> tClass, Object data) {
        String uri = mainApiPath + path;
        restTemplate.put(mainApiPath, data, tClass);
    }
}

