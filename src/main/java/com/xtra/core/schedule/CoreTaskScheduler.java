package com.xtra.core.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.Process;
import com.xtra.core.model.*;
import com.xtra.core.projection.StreamDetailsView;
import com.xtra.core.repository.LineActivityRepository;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import com.xtra.core.service.ApiService;
import com.xtra.core.service.MessagingService;
import com.xtra.core.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.xtra.core.utility.Util.removeQuotations;

@Component
public class CoreTaskScheduler {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final ProgressInfoRepository progressInfoRepository;
    private final LineActivityRepository lineActivityRepository;
    private final MessagingService messagingService;
    private final ApiService apiService;

    @Autowired
    public CoreTaskScheduler(ProcessRepository processRepository, ProcessService processService,
                             StreamInfoRepository streamInfoRepository, ProgressInfoRepository progressInfoRepository,
                             LineActivityRepository lineActivityRepository, MessagingService messagingService, ApiService apiService) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
        this.lineActivityRepository = lineActivityRepository;
        this.messagingService = messagingService;
        this.apiService = apiService;
    }

    @Value("${server.port}")
    private String portNumber;

    @Scheduled(fixedDelay = 10000)
    public void StreamChecker() {
        List<Process> processes = processRepository.findAll();
        processes.parallelStream().forEach((process -> {
            Optional<StreamInfo> infoRecord = streamInfoRepository.findByStreamId(process.getStreamId());
            StreamInfo info = infoRecord.orElseGet(() -> new StreamInfo(process.getStreamId()));
            info = updateStreamUptime(process, info);
            info = updateStreamFFProbeData(process, info);
            streamInfoRepository.save(info);
        }));
    }


    public StreamInfo updateStreamUptime(Process process, StreamInfo info) {
        var uptime = processService.getProcessEtime(process.getPid());
        info.setUptime(uptime);
        return info;
    }

    public StreamInfo updateStreamFFProbeData(Process process, StreamInfo info) {
        String streamUrl = System.getProperty("user.home") + File.separator + "streams" + File.separator + process.getStreamId() + "_.m3u8";
        ProcessOutput processOutput = processService.analyzeStream(streamUrl, "codec_name,width,height,bit_rate");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var root = objectMapper.readTree(processOutput.getOutput());
            var video = root.get("streams").get(0);
            info.setVideoCodec(removeQuotations(video.get("codec_name").toPrettyString()));
            info.setResolution(video.get("width") + "x" + video.get("height"));

            var audio = root.get("streams").get(1);
            if (audio.has("codec_name"))
                info.setAudioCodec(removeQuotations(audio.get("codec_name").toPrettyString()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return info;
    }

    @Scheduled(fixedDelay = 2000)
    public void sendStreamsInfo() {
        List<StreamInfo> streamInfoList = streamInfoRepository.findAll();
        List<ProgressInfo> progressInfoList = progressInfoRepository.findAll();
        List<Process> processes = processRepository.findAll();
        List<StreamDetailsView> streamDetailsViews = new ArrayList<>();
        if (!streamInfoList.isEmpty()) {
            for (var info : streamInfoList) {
                StreamDetailsView status = new StreamDetailsView();
                status.updateStreamInfo(info);
                status.updateProgressInfo(progressInfoList.stream().filter(progressInfo -> progressInfo.getStreamId().equals(info.getStreamId())).findFirst().orElseGet(ProgressInfo::new));
                //add stream status
                status.setStreamStatus(StreamStatus.OFFLINE);
                processes.forEach(process -> {
                    if (process.getStreamId().equals(info.getStreamId())){
                        status.setStreamStatus(StreamStatus.ONLINE);
                    }
                });
                streamDetailsViews.add(status);
            }
            messagingService.sendStreamStatus(streamDetailsViews);
        }
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void removeOldConnections() {
        List<Connection> lineActivities = lineActivityRepository.findAllByLastReadIsLessThanEqual(LocalDateTime.now().minusMinutes(1));
        if (lineActivities.isEmpty())
            return;
        for (Connection activity : lineActivities) {
            lineActivityRepository.deleteById(activity.getId());
        }
        apiService.sendPostRequest("/line_activities/batch_delete", String.class, lineActivities);
    }

    @Scheduled(fixedDelay = 5000)
    public void removeHlsEndedConnections() {
        List<Connection> lineActivities = lineActivityRepository.findAllByHlsEndedAndEndDateBefore(true, LocalDateTime.now().minusMinutes(1));
        if (lineActivities.isEmpty())
            return;
        for (Connection activity : lineActivities) {
            lineActivityRepository.deleteById(activity.getId());
        }
        new RestTemplate().delete("/line_activities/batch_delete", lineActivities);

    }

    @Scheduled(fixedDelay = 5000)
    public void sendStreamActivity() {
        List<Connection> lineActivities = lineActivityRepository.findAll();
        if (!lineActivities.isEmpty()) {
            apiService.sendPostRequest("/line_activities/batch/?portNumber=" + portNumber, String.class, lineActivities);
        }
    }

}
