package com.xtra.core.service;

import com.xtra.core.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StreamServiceIntegrationTest {

    @Autowired
    StreamService streamService;

    @Test
    void startStream() throws Exception {

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

        assertTrue(streamService.startStream(server.getId(), stream));

    }
}