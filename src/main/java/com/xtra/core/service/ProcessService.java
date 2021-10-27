package com.xtra.core.service;

import com.xtra.core.model.ProcessOutput;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sun.jna.Platform.isWindows;

@Service
@Log4j2
public class ProcessService {

    public Long runProcess(String... args) {
        File bitbucket;

        if (isWindows()) {
            bitbucket = new File("NUL");
        } else {
            bitbucket = new File("/dev/null");
        }
        Process proc;
        try {
            proc = new ProcessBuilder(args)
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(bitbucket))
                    .redirectError(ProcessBuilder.Redirect.appendTo(bitbucket))
                    .start();
            log.info("Starting process with args: " + Arrays.toString(args) + "\r\n pid: " + proc.pid());
        } catch (IOException e) {
            log.error("Starting process with args: " + Arrays.toString(args) + " failed");
            return -1L;
        }
        return proc.pid();
    }

    public void stopProcess(Long pid) {
        ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);
    }

    public String getProcessEtime(Long pid) {
        Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
        if (processHandle.isEmpty() || processHandle.get().info().startInstant().isEmpty())
            return null;
        Duration duration = Duration.between(processHandle.get().info().startInstant().get(), Instant.now());
        return DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
    }
    public Duration getProcessDuration(Long pid) {
        Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
        if (processHandle.isEmpty() || processHandle.get().info().startInstant().isEmpty())
            return null;
        return Duration.between(processHandle.get().info().startInstant().get(), Instant.now());
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
                    "2000000"
            ).start();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            output = in.lines().map(Object::toString).collect(Collectors.joining(" "));
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return null;
        }
        return new ProcessOutput(output, proc.exitValue());
    }

    public String getMediaInfo(String sourceInput) {
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