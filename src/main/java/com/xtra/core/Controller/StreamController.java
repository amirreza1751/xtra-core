package com.xtra.core.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StreamController {
    @GetMapping("/auth")
    ResponseEntity<String> authenticateUser(){
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

}
