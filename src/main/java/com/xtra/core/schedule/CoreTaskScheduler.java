package com.xtra.core.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.*;
import com.xtra.core.model.Process;
import com.xtra.core.repository.LineActivityRepository;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import com.xtra.core.service.MainServerApiService;
import com.xtra.core.service.ProcessService;
import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xtra.core.utility.Util.removeQuotations;

@Component
public class CoreTaskScheduler {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final ProgressInfoRepository progressInfoRepository;
    private final LineActivityRepository lineActivityRepository;
    private final MainServerApiService mainServerApiService;
    private final StreamService streamService;

    @Autowired
    public CoreTaskScheduler(ProcessRepository processRepository, ProcessService processService,
                             StreamInfoRepository streamInfoRepository, ProgressInfoRepository progressInfoRepository,
                             LineActivityRepository lineActivityRepository, MainServerApiService mainServerApiService, StreamService streamService) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
        this.lineActivityRepository = lineActivityRepository;
        this.mainServerApiService = mainServerApiService;
        this.streamService = streamService;
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
            if (info.getVideoCodec() == null){
                File streamsDirectory = new File(
                        System.getProperty("user.home") + File.separator + "streams"
                );
                for (File f : streamsDirectory.listFiles()) {
                    if (f.getName().startsWith(process.getStreamId()+ "_")) {
                        f.delete();
                    }
                }
            }
                streamInfoRepository.save(info);
        }));
    }

    public void restartStreamIfStopped(Long streamId) {
        mainServerApiService.sendGetRequest("/channels/" + streamId + "/change-source/?portNumber=" + portNumber, Integer.class);
    }

    public StreamInfo updateStreamUptime(Process process, StreamInfo info) {
        var uptime = processService.getProcessEtime(process.getPid());
        info.setUptime(uptime);
        return info;
    }

    public StreamInfo updateStreamFFProbeData(Process process, StreamInfo info) {
        String streamUrl = System.getProperty("user.home") + File.separator + "streams" + File.separator + process.getStreamId() + "_.m3u8";
        ProcessOutput processOutput = processService.analyzeStream(streamUrl, "codec_name,width,height,bit_rate");
        if (processOutput.getExitValue() == 1){
            restartStreamIfStopped(process.getStreamId());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var root = objectMapper.readTree(processOutput.getOutput());
            var video = root.get("streams").get(0);
            info.setVideoCodec(removeQuotations(video.get("codec_name").toPrettyString()));
            info.setResolution(video.get("width") + "x" + video.get("height"));

            var audio = root.get("streams").get(1);
            if (audio.has("codec_name"))
                info.setAudioCodec(audio.get("codec_name").toPrettyString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        mainServerApiService.sendPostRequest("/channels/stream_info/batch/?portNumber=" + portNumber, String.class, infos);

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
        mainServerApiService.sendPostRequest("/line_activities/batch_delete", String.class, lineActivities);
    }

    @Scheduled(fixedDelay = 5000)
    public void removeHlsEndedConnections() {
        List<LineActivity> lineActivities = lineActivityRepository.findAllByHlsEndedAndEndDateBefore(true, LocalDateTime.now().minusMinutes(1));
        if (lineActivities.isEmpty())
            return;
        for (LineActivity activity : lineActivities) {
            lineActivityRepository.deleteById(activity.getId());
        }
        new RestTemplate().delete("/line_activities/batch_delete", lineActivities);

    }

    @Scheduled(fixedDelay = 5000)
    public void  sendStreamActivity() {
        List<LineActivity> lineActivities = lineActivityRepository.findAll();
        if (!lineActivities.isEmpty()){
            mainServerApiService.sendPostRequest("/line_activities/batch/?portNumber=" +  portNumber, String.class, lineActivities);
        }
    }

}
