package com.xtra.core.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.xtra.core.model.Process;

import java.util.ArrayList;
import java.util.List;

@Component
public class CoreTaskScheduler {
    private final ProcessRepository processRepository;
    private final ProcessService processService;

    @Autowired
    public CoreTaskScheduler(ProcessRepository processRepository, ProcessService processService) {
        this.processRepository = processRepository;
        this.processService = processService;
    }

    @Scheduled(fixedDelay = 5000)
    public void updateStreamInfo() throws JsonProcessingException {
        List<StreamInfo> streamInfoList = new ArrayList<>();
        for (Process process : processRepository.findAll()) {
            var uptime = processService.getProcessEtime(process.getPid());
            String streamUrl = "";
            String streamAnalysis = processService.streamAnalysis(streamUrl, "codec_name,width,height,r_frame_rate,bit_rate");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(streamAnalysis);
            streamInfoList.add(new StreamInfo(process.getStreamId(), uptime, "", "", "", "", "", "", ""));
        }
        //call api here;
    }
}
