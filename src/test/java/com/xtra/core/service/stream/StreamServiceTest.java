package com.xtra.core.service.stream;

import com.xtra.core.model.*;
import com.xtra.core.model.Process;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import com.xtra.core.service.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StreamServiceTest {

    @InjectMocks
    private StreamService streamService;

    @Mock
    private  ProcessRepository processRepository;
    @Mock
    private  ProcessService processService;
    @Mock
    private  StreamInfoRepository streamInfoRepository;
    @Mock
    private  ProgressInfoRepository progressInfoRepository;
    @Mock
    private  LineService lineService;
    @Mock
    private  LineActivityService lineActivityService;
    @Mock
    private  MainServerApiService mainServerApiService;
    @Mock
    private  ServerService serverService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void startStream() {
        Server server = new Server();
        server.setId(12L);

        Stream stream = new Stream();
        stream.setId(6L);

        StreamInput streamInput = new StreamInput();
        streamInput.setUrl("http://tivix.eu:8000/iptenjoyim/3GaALqqI2tcL/62748");
        List<StreamInput> streamInputs = new ArrayList<>();
        streamInputs.add(streamInput);
        stream.setStreamInputs(streamInputs);

        StreamServer streamServer = new StreamServer();
        streamServer.setId(new StreamServerId(stream.getId(), server.getId()));
        streamServer.setSelectedSource(0);

        Set<StreamServer> streamServers = new HashSet<>();
        streamServers.add(streamServer);
        stream.setStreamServers(streamServers);

        ReflectionTestUtils.setField(streamService, "serverAddress" , "localhost");
        ReflectionTestUtils.setField(streamService, "serverPort" , "8081");

        Mockito.doReturn(5L).when(processService).runProcess();
        Mockito.doReturn(new Process(6L, 5L)).when(processRepository).save(Mockito.any());

        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setStreamId(stream.getId());
        Mockito.doReturn(Optional.of(streamInfo)).when(streamInfoRepository).findByStreamId(Mockito.anyLong());

        Mockito.doReturn(streamInfo).when(streamInfoRepository).save(Mockito.any(StreamInfo.class));

        assertTrue(streamService.startStream(12L, stream));
    }

}