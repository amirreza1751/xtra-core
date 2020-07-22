package com.xtra.core.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Service
public class ProcessService {
    public Optional<Process> runProcess(String... args) {
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
        } catch (IOException e) {
            //@todo log
            System.out.println(e.getMessage());
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

    public String getProcessEtime(Long pid) {
        Process proc;
        String output;
        try {
            proc = new ProcessBuilder("ps", "-p", pid.toString(), "-o", "etime=").start();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            output = in.readLine();
        } catch (IOException e) {
            return null;
        }
        return output;
    }

    public String streamAnalysis(String sourceInput, String params){
        Process proc;
        String output;
        try {
            proc = new ProcessBuilder(
                    "ffprobe",
                    "-show_streams",
                    "-show_entries",
                    "stream=" + params,
                    "-of",
                    "json",
                    "-v",
                    "quiet",
                    "-i",
                    sourceInput
            ).start();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            output = in.readLine();
        } catch (IOException e) {
            return null;
        }
        return output;
    }


}
