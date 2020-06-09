package com.xtra.core.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/streams")
public class StreamOpController {

    @GetMapping("/start")
    public int startStream(){
        String[] args = new String[] {"/bin/ffmpeg", "-i", "source"};
        Process proc;
        try {
             proc = new ProcessBuilder(args).start();
        } catch (IOException e) {
            return -1;
        }
        return proc.exitValue();
    }
}
