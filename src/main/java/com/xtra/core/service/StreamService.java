package com.xtra.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.xtra.core.config.DynamicConfig;
import com.xtra.core.dto.ChannelStart;
import com.xtra.core.dto.LineAuth;
import com.xtra.core.dto.StreamDetailsView;
import com.xtra.core.dto.catchup.CatchupRecordView;
import com.xtra.core.mapper.ChannelStartMapper;
import com.xtra.core.mapper.StreamMapper;
import com.xtra.core.model.*;
import com.xtra.core.model.exception.EntityNotFoundException;
import com.xtra.core.repository.CatchUpInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import com.xtra.core.repository.StreamRepository;
import lombok.extern.log4j.Log4j2;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xtra.core.utility.Util.removeQuotations;

@Service
@Log4j2
public class StreamService {
    private final ProcessService processService;
    private final LineService lineService;
    private final ApiService apiService;
    private final CatchUpInfoRepository catchUpInfoRepository;
    private final StreamRepository streamRepository;
    private final StreamInfoRepository streamInfoRepository;
    private final ChannelStartMapper channelStartMapper;
    private final StreamMapper streamMapper;
    private final DynamicConfig config;

    @Value("${main.apiPath}")
    private String mainApiPath;
    @Value("${server.external.address}")
    private String serverAddress;
    @Value("${server.port}")
    private String serverPort;
    @Value("${streams.path}")
    private String streamsPath;
    @Value("${telerecord.path}")
    private String teleRecordPath;

    File streamsDirectory;


    @Autowired
    public StreamService(ProcessService processService, LineService lineService, ApiService apiService,
                         CatchUpInfoRepository catchUpInfoRepository, StreamRepository streamRepository,
                         StreamInfoRepository streamInfoRepository, ChannelStartMapper channelStartMapper, StreamMapper streamMapper, DynamicConfig config) {
        this.processService = processService;
        this.lineService = lineService;
        this.apiService = apiService;
        this.catchUpInfoRepository = catchUpInfoRepository;
        this.streamRepository = streamRepository;
        this.streamInfoRepository = streamInfoRepository;
        this.channelStartMapper = channelStartMapper;
        this.streamMapper = streamMapper;
        this.config = config;
    }

    @PostConstruct
    public void init(){
        streamsDirectory = new File(streamsPath);
        if (!streamsDirectory.exists()) {
            var result = streamsDirectory.mkdirs();
            if (!result) {
                throw new RuntimeException("Could not create directory");
            }
        }
    }

    public void startStream(ChannelStart channelStart) {
        Stream stream = streamRepository.findById(channelStart.getId()).orElse(new Stream());
        stream = channelStartMapper.updateStreamFields(channelStart, stream);
        Long pid = processService.runProcess(getProcessArgsForStream(stream).toArray(new String[0]));
        if (pid != -1L) {
            stream.setPid(pid);
            stream.setStreamInfo(new StreamInfo());
            stream.setProgressInfo(new ProgressInfo());
            streamRepository.save(stream);
        }
    }


    public void stopStream(Long streamId) {
        var stream = streamRepository.findById(streamId).orElseThrow(() -> new EntityNotFoundException("Stream", streamId));
        processService.stopProcess(stream.getPid());
        streamRepository.delete(stream);
    }

    public boolean restartStream(Long streamId) {
        var stream = streamRepository.findById(streamId).orElseThrow(() -> new EntityNotFoundException("Stream", streamId));
        processService.stopProcess(stream.getPid());
        Long pid = processService.runProcess(getProcessArgsForStream(stream).toArray(new String[0]));
        stream.setPid(pid);
        streamRepository.save(stream);
        return true;
    }

    public void startAllStreams(List<ChannelStart> channelStarts) {
        for (var channelStart : channelStarts) {
            startStream(channelStart);
        }
    }

    public void stopAllStreams() {
        killAllStreamProcesses();
        streamRepository.deleteAll();
    }

    public boolean restartAllStreams() {
        List<Stream> streams = streamRepository.findAll();
        killAllStreamProcesses();
        for (var stream : streams) {
            Long pid = processService.runProcess(getProcessArgsForStream(stream).toArray(new String[0]));
            stream.setPid(pid);
        }
        streamRepository.saveAll(streams);
        return true;
    }

    private void killAllStreamProcesses() {
        var processIds = streamRepository.findAllBy();
        for (var pid : processIds) {
            processService.stopProcess(pid.getPid());
        }
    }

    private List<String> getProcessArgsForStream(Stream stream) {
        var advancedOptions = stream.getAdvancedStreamOptions();

        FFmpegBuilder builder = new FFmpegBuilder();
        if (advancedOptions.isNativeFrames())
            builder.addExtraArgs("-re");
        if (advancedOptions.getProbeSize() > 32)
            builder.addExtraArgs("-probesize", String.valueOf(advancedOptions.getProbeSize()));
        if (!StringUtils.isEmpty(advancedOptions.getHttpProxy()))
            builder.addExtraArgs("-http_proxy", "http://" + advancedOptions.getHttpProxy());
        if (!StringUtils.isEmpty(advancedOptions.getHeaders()))
            builder.addExtraArgs("-headers", advancedOptions.getHeaders());
        if (advancedOptions.isGeneratePts())
            builder.addExtraArgs("-fflags", "+genpts");

        FFmpegOutputBuilder fFmpegOutputBuilder = builder
                .addExtraArgs("-nostdin")
                .addExtraArgs("-hide_banner")
                .addProgress(URI.create("http://" + serverAddress + ":" + serverPort + "/update?stream_id=" + stream.getId()))
                .setInput(stream.getStreamInput())
                .addOutput(streamsDirectory + "/" + stream.getId() + "_.m3u8")
                .addExtraArgs("-acodec", "copy")
                .addExtraArgs("-vcodec", "copy")
                .addExtraArgs("-f", "hls")
                .addExtraArgs("-safe", "0")
                .addExtraArgs("-segment_time", "10")
                .addExtraArgs("-hls_flags", "delete_segments+append_list");

        builder = fFmpegOutputBuilder.done();
        return ImmutableList.<String>builder().add(FFmpeg.DEFAULT_PATH).addAll(builder.build()).build();
    }


    public boolean batchStopStreams(List<Long> streamIds) {
        var streams = streamRepository.findAllByIdIn(streamIds);
        for (var stream : streams) {
            processService.stopProcess(stream.getPid());
        }
        streamRepository.deleteAllInBatch(streams);
        return true;
    }


    public Map<String, String> getPlaylist(String lineToken, String streamToken, String extension, String userAgent, String ipAddress) throws IOException {
        Map<String, String> data = new HashMap<>();
        LineStatus status = lineService.authorizeLineForStream(new LineAuth(lineToken, streamToken, ipAddress, userAgent, config.getServerToken()));
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
            var stream = streamRepository.findByStreamToken(streamToken).orElseThrow();
            File file = ResourceUtils.getFile(streamsPath + File.separator + stream.getId() + "_." + extension);
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
        LineStatus status = lineService.authorizeLineForStream(new LineAuth(lineToken, streamToken, ipAddress, userAgent, config.getServerToken()));
        if (status == LineStatus.OK) {
            var stream = streamRepository.findByStreamToken(streamToken).orElseThrow();
            return IOUtils.toByteArray(FileUtils.openInputStream(new File(streamsPath + File.separator + stream.getId() + "_" + segment + "." + extension)));
        } else {
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        }
    }

    //catch-up
    public Boolean record(Long streamId, CatchupRecordView catchupRecordView) {
        var catchUpInfo = catchUpInfoRepository.findByStreamId(streamId);
        if (catchUpInfo.isPresent()) {
            var result = catchUpInfo.get();
            result.setCatchUpDays(catchupRecordView.getCatchUpDays());
            catchUpInfoRepository.save(result);
        } else {
            CatchUpInfo newCatchUp = new CatchUpInfo(streamId, catchupRecordView.getCatchUpDays());
            catchUpInfoRepository.save(newCatchUp);
        }
        File catchUpDirectory = new File(
                teleRecordPath + File.separator + streamId
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
                .addOutput(catchUpDirectory.getAbsolutePath() + "/" + catchupRecordView.getStart() + "_" + catchupRecordView.getStop() + "_" + catchupRecordView.getTitle() + ".mp4")
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
                catchupRecordView.setLocation(catchUpDirectory.getAbsolutePath() + "/" + catchupRecordView.getStart() + "_" + catchupRecordView.getStop() + "_" + catchupRecordView.getTitle() + ".mp4");
                apiService.sendPostRequest("/system/streams/" + streamId + "/recording/false", String.class, catchupRecordView);
            } catch (IOException | InterruptedException e) {
                System.out.println(e.getMessage());
            } finally {
                executor.shutdown();
            }
        });
        return true;
    }


    public void updateStreamInfo() {
        var streams = streamRepository.findAll();
        streams.forEach((stream -> {
            var uptime = processService.getProcessEtime(stream.getPid());
            var info = updateStreamFFProbeData(stream, stream.getStreamInfo());
            info.setUptime(uptime);
            streamInfoRepository.save(info);
        }));
    }

    private StreamInfo updateStreamFFProbeData(Stream stream, StreamInfo info) {
        String streamUrl = streamsPath + File.separator + stream.getId() + "_.m3u8";
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
        } catch (Exception ignored) {
        }
        info.setCurrentInput(stream.getStreamInput());
        return info;
    }

    public List<StreamDetailsView> getStreamDetails() {
        List<StreamDetailsView> streamDetailsViews = new ArrayList<>();
        var streams = streamRepository.findAll();
        for (var stream : streams) {
            StreamDetailsView detailsView = new StreamDetailsView(stream.getId());
            detailsView = streamMapper.copyStreamInfo(stream.getStreamInfo(), detailsView);
            detailsView = streamMapper.copyProgressInfo(stream.getProgressInfo(), detailsView);
            if (detailsView.getLastUpdated() != null && detailsView.getLastUpdated().isBefore(LocalDateTime.now().minusSeconds(10L))){
                detailsView.setStreamStatus(StreamStatus.OFFLINE);
            } else detailsView.setStreamStatus(StreamStatus.ONLINE);
            streamDetailsViews.add(detailsView);
            System.out.println(detailsView.getStreamStatus());
        }
        return streamDetailsViews;
    }
}
