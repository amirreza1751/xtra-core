package com.xtra.core.service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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
        ResponseEntity<T> result = null;
        try {
            result = restTemplate.getForEntity(uri, tClass);
        } catch (HttpClientErrorException | NullPointerException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
        return result.getBody();
    }

    public <T> T sendPostRequest(String path, Class<T> tClass, Object data) {
        String uri = mainApiPath + path;
        ResponseEntity<T> result = null;
        try {
            result = restTemplate.postForEntity(uri, data, tClass);
        } catch (HttpClientErrorException | NullPointerException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
        return result.getBody();
    }

    public <T> void sendPatchRequest(String path, Object data) {
        String uri = mainApiPath + path;
        try {
            restTemplate.patchForObject(uri, data, ResponseEntity.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public <T> void sendDeleteRequest(String path) {
        String uri = mainApiPath + path;
        try {
            restTemplate.delete(uri);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public <T> void sendPutRequest(String path, Object data) {
        String uri = mainApiPath + path;
        try {
            restTemplate.put(URI.create(uri), data);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
        }
    }
}

