package com.xtra.core.schedule;

import com.xtra.core.model.Stream;
import com.xtra.core.model.StreamInfo;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.StreamRepository;
import com.xtra.core.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.xtra.core.model.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CoreTaskScheduler {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamRepository streamRepository;

    @Autowired
    public CoreTaskScheduler(ProcessRepository processRepository, ProcessService processService, StreamRepository streamRepository) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamRepository = streamRepository;
    }

    @Scheduled(fixedDelay = 5000)
    public void updateStreamInfo() {
        List<StreamInfo> streamInfoList = new ArrayList<>();
        for (Process process : processRepository.findAll()) {
            var uptime = processService.getProcessEtime(process.getPid());
            Optional<Stream> stream = streamRepository.findById(process.getStreamId());
            streamInfoList.add(new StreamInfo(process.getStreamId(), uptime, "", "", "", "", "", "", ""));
        }
        //call api here;
    }
}
