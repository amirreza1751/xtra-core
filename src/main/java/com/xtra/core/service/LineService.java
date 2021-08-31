package com.xtra.core.service;

import com.xtra.core.model.LineStatus;
import com.xtra.core.dto.LineAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LineService {
    private final ApiService apiService;

    @Autowired
    public LineService(ApiService apiService) {
        this.apiService = apiService;
    }

    public LineStatus authorizeLineForStream(LineAuth lineAuth) {
        return apiService.sendPostRequest("/system/stream_auth", LineStatus.class, lineAuth);
    }

    public LineStatus authorizeLineForVod(LineAuth lineAuth) {
        return apiService.sendPostRequest("/system/vod_auth/", LineStatus.class, lineAuth);

    }
}
