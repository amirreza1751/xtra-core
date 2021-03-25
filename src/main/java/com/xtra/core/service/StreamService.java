package com.xtra.core.service;

import com.xtra.core.model.*;
import com.xtra.core.model.Process;
import com.xtra.core.projection.LineAuth;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StreamService {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final ProgressInfoRepository progressInfoRepository;
    private final LineService lineService;
    private final LineActivityService lineActivityService;
    private final ApiService apiService;

    @Value("${main.apiPath}")
    private String mainApiPath;
    @Value("${server.address}")
    private String serverAddress;
    @Value("${server.port}")
    private String serverPort;

    @Value("${nginx.address}")
    private String nginxAddress;
    @Value("${nginx.port}")
    private String nginxPort;

    @Autowired
    public StreamService(ProcessRepository processRepository, ProcessService processService, StreamInfoRepository streamInfoRepository, ProgressInfoRepository progressInfoRepository, LineService lineService, LineActivityService lineActivityService, ApiService apiService) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
        this.lineService = lineService;
        this.lineActivityService = lineActivityService;
        this.apiService = apiService;
    }

    public boolean startStream(Stream stream) {
        if (stream == null) {
            System.out.println("Stream is null");
            return false;
        }
        Long streamId = stream.getId();

        Optional<Process> process = processRepository.findByProcessIdStreamId(streamId);
        if (process.isPresent()) {
            System.out.println("Stream is Already started");
            return false;
        }

        File streamsDirectory = new File(
                System.getProperty("user.home") + File.separator + "streams"
        );
        if (!streamsDirectory.exists()) {
            var result = streamsDirectory.mkdirs();
            if (!result) {
                throw new RuntimeException("Could not create directory");
            }
        }

        String currentInput = stream.getStreamInputs().get(stream.getSelectedSource());

        String[] args = new String[]{
                "ffmpeg",
                "-re",
                "-i",
                currentInput,
                "-vcodec",
                "copy",
                "-acodec",
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
                "-fflags",
                "+genpts",
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
                "-progress",
                "http://" + serverAddress + ":" + serverPort + "/update?stream_id=" + streamId,
                "-hls_flags",
                "delete_segments",
                "-segment_list",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_%d.ts",
                streamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_.m3u8"
        };
        Long pid = processService.runProcess(args);
        if (pid == -1L) {
            return false;
        } else {
            processRepository.save(new Process(stream.getId(), pid));
            Optional<StreamInfo> streamInfoRecord = streamInfoRepository.findByStreamId(streamId);
            StreamInfo streamInfo = streamInfoRecord.orElseGet(() -> new StreamInfo(streamId));
            streamInfo.setCurrentInput(currentInput);
            streamInfoRepository.save(streamInfo);
        }
        return true;
    }

    public boolean startStream(Long streamId) { //Overload start stream for starting single stream
        Stream stream = getStream(streamId);
        if (stream == null) {
            System.out.println("Stream is null");
            return false;
        }
        return startStream(stream);
    }

    public boolean startStream(List<Long> streamIds) { //Overload start stream for batch start streams
        List<Stream> streams = getBatchStreams(streamIds).getChannelList();

        for (Stream stream : streams) {
            startStream(stream);
        }
        return true;
    }

    public boolean stopStream(Long streamId) {
        Optional<Process> process = processRepository.findByProcessIdStreamId(streamId);
        if (process.isPresent()) {
            var pid = process.get().getProcessId().getPid();
            processService.stopProcess(pid);
            processRepository.deleteByProcessIdStreamId(streamId);

            Optional<ProgressInfo> progressInfo = progressInfoRepository.findByStreamId(streamId);
            if (progressInfo.isPresent()) {
                progressInfoRepository.deleteById(streamId);
            }
            Optional<StreamInfo> streamInfo = streamInfoRepository.findByStreamId(streamId);
            if (streamInfo.isPresent()) {
                streamInfoRepository.deleteById(streamId);
            }

        } else {
            return false;
        }
        return true;
    }

    public boolean stopStream(List<Long> streamIds) {
        for (Long streamId : streamIds) {
            stopStream(streamId);
        }
        return true;
    }

    public boolean restartStream(Long streamId) {
        this.stopStream(streamId);
        this.startStream(streamId);
        return true;
    }

    public boolean restartStream(List<Long> streamIds) {
        this.stopStream(streamIds);
        this.startStream(streamIds);
        return true;
    }

    public Stream getStream(Long streamId) {
        try {
            return apiService.sendGetRequest("/servers/current/channels/" + streamId + "?port=" + serverPort, Stream.class);
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ChannelList getBatchStreams(List<Long> streamIds) {
        StringJoiner joiner = new StringJoiner(",");
        for (Long streamId : streamIds) {
            joiner.add(streamId.toString());
        }
        try {
            return apiService.sendGetRequest("/channels/batch-get?streamIds=" + joiner.toString(), ChannelList.class);
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public Long getStreamId(String streamToken) {
        try {
            return apiService.sendGetRequest("/channels/get_id/" + streamToken, Long.class);
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }


    public Map<String, String> getPlaylist(String lineToken, String streamToken, String extension, String userAgent, String ipAddress) throws IOException {
        Map<String, String> data = new HashMap<>();
        LineStatus status = lineService.authorizeLineForStream(new LineAuth(lineToken, streamToken, ipAddress, userAgent));
        if (status != LineStatus.OK) {
            if (status == LineStatus.NOT_FOUND)
                throw new RuntimeException("Line Not found " + HttpStatus.NOT_FOUND);
            else if (status == LineStatus.BANNED)
                throw new RuntimeException("Line is Banned " + HttpStatus.FORBIDDEN);
            else if (status == LineStatus.BLOCKED)
                throw new RuntimeException("Line is Blocked " + HttpStatus.FORBIDDEN);
            else if (status == LineStatus.EXPIRED)
                throw new RuntimeException("Line is Expired, Please Extend Your Line " + HttpStatus.FORBIDDEN);
            else if (status == LineStatus.MAX_CONNECTION_REACHED)
                throw new RuntimeException("You Have Used All of your connection capacity " + HttpStatus.FORBIDDEN);
            else if (status == LineStatus.NO_ACCESS_TO_STREAM)
                throw new RuntimeException("Cannot Access Stream " + HttpStatus.FORBIDDEN);
            else
                throw new RuntimeException("Unknown Error " + HttpStatus.FORBIDDEN);
        } else {
            Long lineId = lineService.getLineId(lineToken);
            Long streamId = this.getStreamId(streamToken);
            if (lineId == null || streamId == null) {
//                return new ResponseEntity<>("Unknown Error", HttpStatus.FORBIDDEN);
                throw new RuntimeException("Unknown Error " + HttpStatus.FORBIDDEN);
            }
            ConnectionId connectionId = new ConnectionId(lineId, streamId, ipAddress);
            System.out.println("line id:" + connectionId.getLineId() + " stream id:" + connectionId.getStreamId() + " user Ip" + connectionId.getUserIp());

            var result = lineActivityService.updateLineActivity(connectionId, userAgent);

            if (!result) {
                throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
            }

            File file = ResourceUtils.getFile(System.getProperty("user.home") + "/streams/" + streamId + "_." + extension);
            String playlist = new String(Files.readAllBytes(file.toPath()));

            Pattern pattern = Pattern.compile("(.*)\\.ts");
            Matcher match = pattern.matcher(playlist);

            while (match.find()) {
                String link = match.group(0);
                playlist = playlist.replace(match.group(0), String.format(nginxAddress + ":" + nginxPort + "/hls/%s/%s/%s", lineToken, streamToken, link.split("_")[1]));
            }

            data.put("fileName", file.getName());
            data.put("playlist", playlist);
        }
        return data;
    }


    public byte[] getSegment(String lineToken, String streamToken
            , String extension, String segment, String userAgent, String ipAddress) throws IOException {
        LineStatus status = lineService.authorizeLineForStream(new LineAuth(lineToken, streamToken, ipAddress, userAgent));
        Long streamId = this.getStreamId(streamToken);
        Long lineId = lineService.getLineId(lineToken);
        ConnectionId connectionId = new ConnectionId(lineId, streamId, ipAddress);
        if (status == LineStatus.OK) {
            System.out.println("line id:" + connectionId.getLineId() + " stream id:" + connectionId.getStreamId() + " user Ip" + connectionId.getUserIp());
            var result = lineActivityService.updateLineActivity(connectionId, userAgent);
            if (!result) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
            }
            return IOUtils.toByteArray(FileUtils.openInputStream(new File(System.getProperty("user.home") + "/streams/" + streamId + "_" + segment + "." + extension)));
        } else {
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        }
    }


}
