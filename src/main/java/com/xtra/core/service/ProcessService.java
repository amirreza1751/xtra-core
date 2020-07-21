package com.xtra.core.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class ProcessService {
    public Optional<Process> runProcess(String... args) {
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
        } catch (IOException e) {
            //@todo log
            return Optional.empty();
        }
        return Optional.of(proc);
    }

    public long stopProcess(Long pid) {
        Process proc;
        try {
            proc = new ProcessBuilder("kill", pid.toString()).start();
        } catch (IOException e) {
            return -1;
        }
        return proc.pid();
    }
}
