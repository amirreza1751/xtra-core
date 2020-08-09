package com.xtra.core.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.Process;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.Stream;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import com.xtra.core.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

@Component
public class CoreTaskScheduler {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final ProgressInfoRepository progressInfoRepository;

    @Value("${main.apiPath}")
    private String mainApiPath;

    @Autowired
    public CoreTaskScheduler(ProcessRepository processRepository, ProcessService processService, StreamInfoRepository streamInfoRepository, ProgressInfoRepository progressInfoRepository) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
    }

    @Scheduled(fixedDelay = 1000)
    public void StreamChecker() {
        List<Process> processes = processRepository.findAll();
        processes.parallelStream().forEach((process -> {
            restartStreamIfStopped(process);
            Optional<StreamInfo> infoRecord = streamInfoRepository.findByStreamId(process.getStreamId());
            StreamInfo info = infoRecord.orElseGet(() -> new StreamInfo(process.getStreamId()));
            info = updateStreamUptime(process, info);
            info = updateStreamFFProbeData(process, info);
            streamInfoRepository.save(info);
        }));

    }

    public void restartStreamIfStopped(Process process) {

    }

    public StreamInfo updateStreamUptime(Process process, StreamInfo info) {
        var uptime = processService.getProcessEtime(process.getPid());
        info.setUptime(uptime);
        return info;
    }

    public StreamInfo updateStreamFFProbeData(Process process, StreamInfo info) {
        String streamUrl = System.getProperty("user.home") + File.separator + "streams" + File.separator + process.getStreamId() + "_.m3u8";
        String videoAnalysis = processService.analyzeStream(streamUrl, "codec_name,width,height,bit_rate");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var root = objectMapper.readTree(videoAnalysis);
            var video = root.get("streams").get(0);
            info.setVideoCodec(video.get("codec_name").toPrettyString());
            info.setResolution(video.get("width") + "x" + video.get("height"));

            var audio = root.get("streams").get(1);
            info.setAudioCodec(audio.get("codec_name").toPrettyString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return info;
    }

    @Scheduled(fixedDelay = 2000)
    public void sendStreamsInfo() {
        List<StreamInfo> streamInfoList = streamInfoRepository.findAll();
        List<ProgressInfo> progressInfoList = progressInfoRepository.findAll();

        if (streamInfoList.isEmpty() && progressInfoList.isEmpty())
            return;
        Map<String, Object> infos = new HashMap<>();
        infos.put("streamInfoList", streamInfoList);
        infos.put("progressInfoList", progressInfoList);

        try {
            new RestTemplate().postForObject(mainApiPath + "/streams/updateStreamInfo", infos, Stream.class);
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendStreamActivity() {

    }

}
