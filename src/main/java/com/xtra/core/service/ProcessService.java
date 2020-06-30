package com.xtra.core.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProcessService {
    public long runProcess(String... args) {
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
        } catch (IOException e) {
            return -1;
        }
        return proc.pid();
    }

    public long stopProcess(Long pid) {
        Process proc;
        try {
            proc = new ProcessBuilder("pkill", pid.toString()).start();
        } catch (IOException e) {
            return -1;
        }
        return proc.pid();
    }
}
