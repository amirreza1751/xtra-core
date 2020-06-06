package com.xtra.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class StreamController {
    @GetMapping("/auth")
    ResponseEntity<String> authenticateUser(){
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/live/{streamId}")
    ResponseEntity<Resource> getPlaylist(@PathVariable String streamId){
       /* InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType()
                .body(resource);*/
        return null;
    }



}
