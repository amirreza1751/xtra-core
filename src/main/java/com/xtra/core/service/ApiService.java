package com.xtra.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
public class ApiService {
    private final RestTemplate restTemplate;
    @Value("${main.apiPath}")
    private String mainApiPath;

    public ApiService() {
        this.restTemplate = new RestTemplate();
    }

    public <T> T sendGetRequest(String path, Class<T> tClass) {
        String uri = mainApiPath + path;
        ResponseEntity<T> result;
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
        ResponseEntity<T> result;
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
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
        restTemplate.setRequestFactory(requestFactory);
        List<HttpMessageConverter<?>> httpMessageConverters = restTemplate.getMessageConverters();
        httpMessageConverters.add(mappingJackson2HttpMessageConverter);
        restTemplate.setMessageConverters(httpMessageConverters);
        try {
             restTemplate.patchForObject(uri, data, String.class);
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

