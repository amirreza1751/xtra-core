package com.xtra.core.service;

import com.xtra.core.model.ProcessOutput;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.StreamInfoRepository;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProcessService {
    private final StreamInfoRepository streamInfoRepository;
    private final ProcessRepository processRepository;

    public ProcessService(StreamInfoRepository streamInfoRepository, ProcessRepository processRepository) {
        this.streamInfoRepository = streamInfoRepository;
        this.processRepository = processRepository;
    }

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
            var process = processRepository.findByProcessIdPid(pid).get();
            var streamInfo = streamInfoRepository.findByStreamId(process.getStreamId()).get();
            streamInfo.setUptime(DurationFormatUtils.formatDuration(0, "H:mm:ss"));
            streamInfoRepository.save(streamInfo);
        } catch (IOException e) {
            return -1;
        }
        return proc.pid();
    }

    public String getProcessEtime(Long pid) {
        Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
        if (processHandle.isEmpty() || processHandle.get().info().startInstant().isEmpty())
            return null;
        Duration duration = Duration.between(processHandle.get().info().startInstant().get(), Instant.now());
        return DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
    }

    public ProcessOutput analyzeStream(String sourceInput, String params) {
        Process proc;
        String output = "";
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
                    sourceInput,
                    "-analyzeduration",
                    "500000"
            ).start();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            output = in.lines().map(Object::toString).collect(Collectors.joining(" "));
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return null;
        }
        return new ProcessOutput(output, proc.exitValue());
    }

    public String getMediaInfo(String sourceInput){
        Process proc;
        String output = "";
        try {
            proc = new ProcessBuilder(
                    "ffprobe",
                    "-show_streams",
                    "-show_format",
                    "-of",
                    "json",
                    "-v",
                    "quiet",
                    "-i",
                    sourceInput
            ).start();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            output = in.lines().map(Object::toString).collect(Collectors.joining(" "));
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return null;
        }
        return output;
    }
}