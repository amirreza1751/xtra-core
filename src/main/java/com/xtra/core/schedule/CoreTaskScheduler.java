package com.xtra.core.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.Process;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.StreamInfoRepository;
import com.xtra.core.service.ProcessService;
import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class CoreTaskScheduler {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final StreamService streamService;

    @Autowired
    public CoreTaskScheduler(ProcessRepository processRepository, ProcessService processService, StreamInfoRepository streamInfoRepository, StreamService streamService) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.streamService = streamService;
    }

    @Scheduled(fixedDelay = 2000)
    public void StreamChecker() {
        List<Process> processes = processRepository.findAll();
        processes.parallelStream().forEach((process -> {
            restartStreamIfStopped(process);

            StreamInfo info = new StreamInfo(process.getStreamId());
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
        var uptime = processService.getProcessEtime(process.getStreamId());
        info.setUptime(uptime);
        String currentSource = streamService.getStream(process.getStreamId()).getCurrentInput().getUrl();
        info.setCurrentInput(currentSource);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var root = objectMapper.readTree(videoAnalysis);
            var video = root.get("streams").get(0);
            info.setVideoCodec(video.get("codec_name").toPrettyString());
            info.setResolution(video.get("width") + "x" + video.get("height"));

            var audio = root.get("streams").get(1);
            info.setAudioCodec(audio.get("codec_name").toPrettyString());
//            System.out.println(audio.toPrettyString());
            //info.setBitrate(audio.get("bit_rate").toPrettyString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return info;
    }

    @Scheduled(fixedDelay = 5000)
    public void sendStreamInfo() {
        List<StreamInfo> streamInfoList = streamInfoRepository.findAll();
        //call api here;
    }

}
