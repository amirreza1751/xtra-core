package com.xtra.core.service;

import com.xtra.core.model.Process;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.Stream;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Optional;

@Service
public class StreamService {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final ProgressInfoRepository progressInfoRepository;

    @Value("${main.apiPath}")
    private String mainApiPath;
    @Value("${server.address}")
    private String serverAddress;
    @Value("${server.port}")
    private String serverPort;

    @Autowired
    public StreamService(ProcessRepository processRepository, ProcessService processService, StreamInfoRepository streamInfoRepository, ProgressInfoRepository progressInfoRepository) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
    }

    public boolean startStream(Long streamId) {
        Stream stream = getStream(streamId);
        if (stream == null) {
            System.out.println("Stream is null");
            return false;
        }

        Optional<Process> process = processRepository.findByProcessIdStreamId(streamId);
        if (process.isPresent()) {
            System.out.println("Stream is Already started");
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

        String currentInput = stream.getStreamInputs().get(0).getUrl();

        String[] args = new String[]{
                "ffmpeg",
                "-re",
                "-i",
                currentInput,
                "-vcodec",
                "copy",
                "-acodec",
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
                "-fflags",
                "+genpts",
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
                "-progress",
                "http://" + serverAddress + ":" + serverPort + "/update?stream_id=" + streamId,
                "-hls_flags",
                "delete_segments",
                "-segment_list",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_%d.ts",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_.m3u8"
        };
        Optional<java.lang.Process> result = processService.runProcess(args);

        if (result.isEmpty() || !result.get().isAlive()) {
            return false;
        } else {
            processRepository.save(new Process(stream.getId(), result.get().pid()));
            Optional<StreamInfo> streamInfoRecord = streamInfoRepository.findByStreamId(streamId);
            StreamInfo streamInfo = streamInfoRecord.orElseGet(() -> new StreamInfo(streamId));
            streamInfo.setCurrentInput(currentInput);
            streamInfoRepository.save(streamInfo);
        }
        return true;
    }

    public boolean stopStream(Long streamId) {
        Optional<Process> process = processRepository.findByProcessIdStreamId(streamId);
        if (process.isPresent()) {
            var pid = process.get().getProcessId().getPid();
            processService.stopProcess(pid);
            processRepository.deleteByProcessIdStreamId(streamId);

            Optional<ProgressInfo> progressInfo = progressInfoRepository.findByStreamId(streamId);
            if (progressInfo.isPresent()){
                progressInfoRepository.deleteById(streamId);
            }
            Optional<StreamInfo> streamInfo = streamInfoRepository.findByStreamId(streamId);
            if (streamInfo.isPresent()){
                streamInfoRepository.deleteById(streamId);
            }

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
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }

    public Long getStreamId(String streamToken){
        try {
            return new RestTemplate().getForObject(mainApiPath + "/streams/get_id" + streamToken, Long.class);
        } catch (RestClientException e) {
            //@todo log exception
            return null;
        }
    }
}
