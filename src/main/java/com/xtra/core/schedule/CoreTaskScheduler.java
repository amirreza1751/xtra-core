package com.xtra.core.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.Process;
import com.xtra.core.model.*;
import com.xtra.core.repository.LineActivityRepository;
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

import javax.transaction.Transactional;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CoreTaskScheduler {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final ProgressInfoRepository progressInfoRepository;
    private final LineActivityRepository lineActivityRepository;

    @Value("${main.apiPath}")
    private String mainApiPath;

    @Autowired
    public CoreTaskScheduler(ProcessRepository processRepository, ProcessService processService,
                             StreamInfoRepository streamInfoRepository, ProgressInfoRepository progressInfoRepository,
                             LineActivityRepository lineActivityRepository) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
        this.lineActivityRepository = lineActivityRepository;
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
            if (audio.has("codec_name"))
                info.setAudioCodec(audio.get("codec_name").toPrettyString());
        } catch (Exception e) {
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
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void removeOldConnections() {
        List<LineActivity> lineActivities = lineActivityRepository.findAllByLastReadIsLessThanEqual(LocalDateTime.now().minusMinutes(1));
        if (lineActivities.isEmpty())
            return;
        for (LineActivity activity : lineActivities) {
            lineActivityRepository.deleteById(activity.getId());
        }
        try {
            new RestTemplate().delete(mainApiPath + "/line_activities/batch", lineActivities);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void removeHlsEndedConnections() {
        List<LineActivity> lineActivities = lineActivityRepository.findAllByHlsEndedAndEndDateBefore(true, LocalDateTime.now().minusMinutes(1));
        if (lineActivities.isEmpty())
            return;
        for (LineActivity activity : lineActivities) {
            lineActivityRepository.deleteById(activity.getId());
        }
        try {
            new RestTemplate().delete(mainApiPath + "/line_activities/batch", lineActivities);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void sendStreamActivity() {
        List<LineActivity> lineActivities = lineActivityRepository.findAll();
        if (lineActivities.isEmpty())
            return;
        try {
            new RestTemplate().put(mainApiPath + "/line_activities/batch", lineActivities);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }

}
