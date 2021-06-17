package com.xtra.core.service;

import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.exception.EntityNotFoundException;
import com.xtra.core.repository.ProgressInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Scanner;

@Service
public class ProgressInfoService {
    private final ProgressInfoRepository progressInfoRepository;

    @Autowired
    public ProgressInfoService(ProgressInfoRepository progressInfoRepository) {
        this.progressInfoRepository = progressInfoRepository;
    }

    public void updateProgressInfo(Long streamId, InputStream dataStream) {
        Scanner s = new Scanner(dataStream).useDelimiter("\\s");
        ProgressInfo progressInfo = progressInfoRepository.findByStreamId(streamId).orElseThrow(() -> new EntityNotFoundException("stream", streamId));
        while (s.hasNextLine()) {
            var property = s.nextLine();
            var splitProps = property.split("=");
            switch (splitProps[0]) {
                case "fps":
                    progressInfo.setFrameRate(splitProps[1]);
                    progressInfoRepository.save(progressInfo);
                    break;
                case "bitrate":
                    progressInfo.setBitrate(splitProps[1]);
                    break;
                case "speed":
                    progressInfo.setSpeed(splitProps[1]);
                    break;

            }
        }
    }
}
