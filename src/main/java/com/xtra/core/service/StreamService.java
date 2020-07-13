package com.xtra.core.service;

import com.xtra.core.model.Process;
import com.xtra.core.model.Stream;
import com.xtra.core.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class StreamService {
    private final ProcessRepository processRepository;
    private final ProcessService processService;

    @Autowired
    public StreamService(ProcessRepository processRepository, ProcessService processService) {
        this.processRepository = processRepository;
        this.processService = processService;
    }

    public long StartStream(Stream stream) {

        File streamsDirectory = new File(
                System.getProperty("user.home") + File.separator + "streams"
        );
        if (!streamsDirectory.exists()) {
            var result = streamsDirectory.mkdirs();
            if (!result) {
                throw new RuntimeException("Could not create directory");
            }
        }

        String[] args = new String[]{
                "ffmpeg",
                "-re",
                "-i",
                stream.getCurrentInput().getUrl(),
                "-vcodec",
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
                "-hls_flags",
                "delete_segments",
                "-segment_list",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_%d.ts",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_.m3u8"
        };
        long result = processService.runProcess(args);
        if (result == -1) {
            throw new RuntimeException("Could not create Process");
        } else {
            processRepository.save(new Process(stream.getId(), result));
        }
        return result;
    }

    public boolean StopStream(Long streamId) {
        Optional<Process> process = processRepository.findByProcessIdStreamId(streamId);
        if (process.isPresent()) {
            processService.stopProcess(process.get().getProcessId().getPid());
        } else {
            throw new RuntimeException("Process Could not be found");
        }
        return true;
    }

    public long RestartStream(Stream stream) {
        long pid = 0;
        if (this.StopStream(stream.getId())) {
            pid = this.StartStream(stream);
        }
        return pid;
    }
}
