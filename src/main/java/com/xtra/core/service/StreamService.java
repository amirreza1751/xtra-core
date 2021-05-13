package com.xtra.core.service;

import com.google.common.collect.ImmutableList;
import com.xtra.core.mapper.AdvancedStreamOptionsMapper;
import com.xtra.core.model.*;
import com.xtra.core.model.Process;
import com.xtra.core.projection.ClassifiedStreamOptions;
import com.xtra.core.projection.LineAuth;
import com.xtra.core.projection.catchup.CatchupRecordView;
import com.xtra.core.repository.ConfigurationRepository;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StreamService {
    private final ProcessRepository processRepository;
    private final ProcessService processService;
    private final StreamInfoRepository streamInfoRepository;
    private final ProgressInfoRepository progressInfoRepository;
    private final LineService lineService;
    private final ConnectionService connectionService;
    private final ApiService apiService;
    private final AdvancedStreamOptionsMapper advancedStreamOptionsMapper;
    private final ConfigurationRepository configurationRepository;

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
    public StreamService(ProcessRepository processRepository, ProcessService processService, StreamInfoRepository streamInfoRepository, ProgressInfoRepository progressInfoRepository, LineService lineService, ConnectionService connectionService, ApiService apiService, AdvancedStreamOptionsMapper advancedStreamOptionsMapper, ConfigurationRepository configurationRepository) {
        this.processRepository = processRepository;
        this.processService = processService;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
        this.lineService = lineService;
        this.connectionService = connectionService;
        this.apiService = apiService;
        this.advancedStreamOptionsMapper = advancedStreamOptionsMapper;
        this.configurationRepository = configurationRepository;
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

        ClassifiedStreamOptions classifiedStreamOptions = advancedStreamOptionsMapper.convertToClassified(stream.getAdvancedStreamOptions());

        FFmpegBuilder builder = new FFmpegBuilder();
        if (classifiedStreamOptions != null && classifiedStreamOptions.getInputFlags() != null)
            builder.addExtraArgs(classifiedStreamOptions.getInputFlags());
        if (classifiedStreamOptions != null && classifiedStreamOptions.getInputKeyValues() != null)
            builder.addExtraArgs(classifiedStreamOptions.getInputKeyValues());

        FFmpegOutputBuilder fFmpegOutputBuilder = builder.setInput(currentInput)
                .addOutput(streamsDirectory.getAbsolutePath() + "/" + stream.getStreamToken() + "_.m3u8")
                .addExtraArgs("-acodec", "copy")
                .addExtraArgs("-vcodec", "copy")
                .addExtraArgs("-f", "hls")
                .addExtraArgs("-safe", "0")
                .addExtraArgs("-segment_time", "10")
                .addExtraArgs("-hls_flags", "delete_segments+append_list");
        if (classifiedStreamOptions != null && classifiedStreamOptions.getOutputFlags() != null)
            fFmpegOutputBuilder.addExtraArgs(classifiedStreamOptions.getOutputFlags());
        if (classifiedStreamOptions != null && classifiedStreamOptions.getOutputKeyValues() != null)
            fFmpegOutputBuilder.addExtraArgs(classifiedStreamOptions.getOutputKeyValues());
        builder = fFmpegOutputBuilder.done();
        builder.addProgress(URI.create("http://" + serverAddress + ":" + serverPort + "/update?stream_id=" + streamId));
        List<String> args = builder.build();
        List<String> newArgs =
                ImmutableList.<String>builder().add(FFmpeg.DEFAULT_PATH).addAll(args).build();
        //print the command
        for (String item : newArgs) {
            System.out.print(item + " ");
        }
        System.out.println("");
        //print the command

        Long pid = processService.runProcess(newArgs.toArray(new String[0]));
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

    public boolean restartStream(Long streamId) {
        this.stopStream(streamId);
        this.startStream(streamId);
        return true;
    }

    public boolean startAllStreams() { //Overload start stream for batch start streams
        List<Stream> streams = getBatchStreams().getChannelList();
        for (Stream stream : streams) {
            startStream(stream);
        }
        return true;
    }

    public boolean stopAllStreams() {
        var processes = processRepository.findAll();
        processes.forEach(process -> {
            stopStream(process.getStreamId());
        });
        return true;
    }

    public boolean restartAllStreams() {
        this.stopAllStreams();
        this.startAllStreams();
        return true;
    }

    public boolean batchStopStreams(List<Long> streamIds) {
        var processes = processRepository.findByProcessIdStreamIdIn(streamIds);
        if (processes.size() > 0) {
            processes.forEach(process -> {
                this.stopStream(process.getStreamId());
            });
        }
        return true;
    }


    public Stream getStream(Long streamId) {
        try {
            var token = configurationRepository.findById("token").orElseThrow();
            //@// TODO: 4/22/21 write general api calling with headers
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("token", token.getValue());
            HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
            return restTemplate.exchange(mainApiPath + "/servers/current/channels/" + streamId + "?port=" + serverPort, HttpMethod.GET, entity, Stream.class).getBody();
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ChannelList getBatchStreams() {
        try {
            return apiService.sendGetRequest("/servers/current/channels", ChannelList.class);
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
            var result = connectionService.updateConnection(lineToken, streamToken, ipAddress, userAgent);

            if (!result) {
                throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
            }

            File file = ResourceUtils.getFile(System.getProperty("user.home") + "/streams/" + streamToken + "_." + extension);
            String playlist = new String(Files.readAllBytes(file.toPath()));

            Pattern pattern = Pattern.compile("(.*)\\.ts");
            Matcher match = pattern.matcher(playlist);

            while (match.find()) {
                String link = match.group(0);
                playlist = playlist.replace(match.group(0), String.format("http://" + serverAddress + ":" + serverPort + "/hls/%s/%s/%s", lineToken, streamToken, link.split("_")[1]));
            }

            data.put("fileName", file.getName());
            data.put("playlist", playlist);
        }
        return data;
    }


    public byte[] getSegment(String lineToken, String streamToken
            , String extension, String segment, String userAgent, String ipAddress) throws IOException {
        LineStatus status = lineService.authorizeLineForStream(new LineAuth(lineToken, streamToken, ipAddress, userAgent));
        Long lineId = lineService.getLineId(lineToken);
        if (status == LineStatus.OK) {
            var result = connectionService.updateConnection(lineToken, streamToken, ipAddress, userAgent);
            if (!result) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
            }
            return IOUtils.toByteArray(FileUtils.openInputStream(new File(System.getProperty("user.home") + "/streams/" + streamToken + "_" + segment + "." + extension)));
        } else {
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        }
    }

    //catch-up
    public Boolean record(Long streamId, CatchupRecordView catchupRecordView) {
        File catchUpDirectory = new File(
                System.getProperty("user.home") + File.separator + "tv_archive" + File.separator + streamId
        );
        if (!catchUpDirectory.exists()) {
            var result = catchUpDirectory.mkdirs();
            if (!result) {
                throw new RuntimeException("Could not create directory");
            }
        }
        var programLength = ChronoUnit.SECONDS.between(catchupRecordView.getStart(), catchupRecordView.getStop());
        FFmpegBuilder builder = new FFmpegBuilder();
        builder.setInput(catchupRecordView.getStreamInput())
                .addOutput(catchUpDirectory.getAbsolutePath() + "/" + catchupRecordView.getStart() + "_" + catchupRecordView.getStop() + "_" + catchupRecordView.getTitle() + ".ts")
                .addExtraArgs("-acodec", "copy")
                .addExtraArgs("-vcodec", "copy")
                .addExtraArgs("-t", Long.toString(programLength))
                .done();
//                .addProgress(URI.create("http://" + serverAddress + ":" + serverPort + "/update?stream_id=" + streamId));
        List<String> args = builder.build();
        List<String> newArgs =
                ImmutableList.<String>builder().add(FFmpeg.DEFAULT_PATH).addAll(args).build();
        //print the command
        for (String item : newArgs) {
            System.out.print(item + " ");
        }
        System.out.println("");
        //print the command

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            java.lang.Process proc;
            try {
                proc = new ProcessBuilder(newArgs).start();
                proc.waitFor();
                if (proc.exitValue() == 1) {
                    throw new RuntimeException("Recording failed.");
                }
                apiService.sendGetRequest("/catch-up/streams/" + streamId + "/recording/false", String.class);
            } catch (IOException | InterruptedException e) {
                System.out.println(e.getMessage());
            } finally {
                executor.shutdown();
            }
        });
        return true;
    }

}
