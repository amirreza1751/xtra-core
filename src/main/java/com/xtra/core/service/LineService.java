package com.xtra.core.service;

import com.xtra.core.model.Line;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class LineService {
    public ResponseEntity<String> authorizeLine(int streamId, int userId) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            Line line = restTemplate.getForObject("http://localhost:8082/api/lines/" + userId, Line.class);
            if (line.getExpireDate().compareTo(LocalDateTime.now()) < 0) {
                //Line Expired
                return new ResponseEntity<>("Line is Expired", HttpStatus.FORBIDDEN);
            } else if (line.isBlocked()) {
                //Line is blocked
                return new ResponseEntity<>("Line is Blocked", HttpStatus.FORBIDDEN);
            } else if (line.isAdminBlocked()) {
                //Line is admin blocked
                return new ResponseEntity<>("Line is Admin Blocked", HttpStatus.FORBIDDEN);
            } else //@todo check if line has access to stream
                //Play Stream
                return new ResponseEntity<>("Play", HttpStatus.ACCEPTED);
        } catch (HttpStatusCodeException exception) {
            int statusCode = exception.getStatusCode().value();
            if (statusCode == 404) {
                return new ResponseEntity<>("Line not Found", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
