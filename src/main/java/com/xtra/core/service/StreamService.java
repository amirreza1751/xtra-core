package com.xtra.core.service;

import com.xtra.core.model.Stream;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class StreamService {
    public String StartStream(Stream stream){
        String[] args = new String[] {"/bin/ffmpeg", "-i", "test"};
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
        } catch (IOException e) {
            return "failed";
        }
        return "done";
    }
}
