package com.xtra.core.service;

import com.xtra.core.model.Process;
import com.xtra.core.model.Stream;
import com.xtra.core.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Optional;

@Service
public class StreamService {
    private final ProcessRepository processRepository;
    private final ProcessService processService;

    @Value("${main.apiPath}")
    private String mainApiPath;

    @Autowired
    public StreamService(ProcessRepository processRepository, ProcessService processService) {
        this.processRepository = processRepository;
        this.processService = processService;
    }

    public boolean startStream(Long streamId) {
        Stream stream = getStream(streamId);
        if (stream == null){
            System.out.println("stream is null");
            return false;
        }

        Optional<Process> process = processRepository.findByProcessIdStreamId(streamId);
        if (process.isPresent()) {
            System.out.println("Process is not present");
            return false;
        }

        File streamsDirectory = new File(
                System.getProperty("user.home") + File.separator + "streams"
        );
        if (!streamsDirectory.exists()) {
            var result = streamsDirectory.mkdirs();
            if (!result) {
                throw new RuntimeException("Could not create directory");
            }
        }

        String[] args = new String[]{
                "ffmpeg",
                "-re",
                "-i",
                stream.getCurrentInput().getUrl(),
                "-vcodec",
                "copy",
                "-loop",
                "-1",
                "-c:a",
                "aac",
                "-b:a",
                "160k",
                "-ar",
                "44100",
                "-strict",
                "-2",
                "-f",
                "hls",
                "-segment_format",
                "mpegts",
                "-segment_time",
                "10",
                "-segment_list_size",
                "6",
                "-segment_format_options",
                "mpegts_flags=+initial_discontinuity:mpegts_copyts=1",
                "-segment_list_type",
                "m3u8",
                "-hls_flags",
                "delete_segments",
                "-segment_list",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_%d.ts",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_.m3u8"
        };
        Optional<java.lang.Process> result = processService.runProcess(args);
        if (result.isEmpty()) {
            return false;
        } else {
            processRepository.save(new Process(stream.getId(), result.get().pid()));
        }
        return true;
    }

    public boolean stopStream(Long streamId) {
        Optional<Process> process = processRepository.findByProcessIdStreamId(streamId);
        if (process.isPresent()) {
            var pid = process.get().getProcessId().getPid();
            processService.stopProcess(pid);
            processRepository.deleteByProcessIdStreamId(streamId);
        } else {
            return false;
        }
        return true;
    }

    public boolean restartStream(Long streamId) {
        this.stopStream(streamId);
        this.startStream(streamId);
        return true;
    }

    public Stream getStream(Long streamId) {
        try {
            return new RestTemplate().getForObject(mainApiPath + "/streams/" + streamId, Stream.class);
        } catch (HttpClientErrorException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }
}
