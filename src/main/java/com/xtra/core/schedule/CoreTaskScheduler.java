package com.xtra.core.schedule;

import com.xtra.core.model.CatchUpInfo;
import com.xtra.core.repository.CatchUpInfoRepository;
import com.xtra.core.service.FileSystemService;
import com.xtra.core.service.MessagingService;
import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CoreTaskScheduler {
    private final StreamService streamService;
    private final MessagingService messagingService;
    private final CatchUpInfoRepository catchUpInfoRepository;
    private final FileSystemService fileSystemService;


    @Autowired
    public CoreTaskScheduler(StreamService streamService, MessagingService messagingService,
                             CatchUpInfoRepository catchUpInfoRepository,
                             FileSystemService fileSystemService) {
        this.streamService = streamService;
        this.messagingService = messagingService;
        this.catchUpInfoRepository = catchUpInfoRepository;
        this.fileSystemService = fileSystemService;
    }

    @Scheduled(fixedDelay = 10000)
    public void StreamChecker() {
        streamService.updateStreamInfo();
    }

    @Scheduled(fixedDelay = 2000)
    public void sendStreamsInfo() {
        messagingService.sendStreamStatus(streamService.getStreamDetails());
    }

    @Scheduled(cron = "0 0 */1 * * *") // Hourly
    public void removeOldRecordings() {
        var infos = catchUpInfoRepository.findAll();
        if (infos.size() > 0) {
            for (CatchUpInfo catchUpInfo : infos) {
                fileSystemService.deleteOldFilesAfterDays(catchUpInfo.getCatchUpDays(), ".ts", System.getProperty("user.home") + File.separator + "tv_archive" + File.separator + catchUpInfo.getStreamId());
            }
        }
    }
}
