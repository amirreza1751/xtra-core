package com.xtra.core.integration;

import com.xtra.core.model.Stream;
import com.xtra.core.model.StreamInput;
import com.xtra.core.service.StreamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StreamServiceIntegrationTest {

    @Autowired
    StreamService streamService;

    /*@Test
    void startStream() {

        Stream stream = new Stream();
        stream.setId(6L);

        StreamInput streamInput = new StreamInput();
        streamInput.setUrl("http://tivix.eu:8000/iptenjoyim/3GaALqqI2tcL/62748");
        List<StreamInput> streamInputs = new ArrayList<>();
        streamInputs.add(streamInput);
//        stream.setStreamInputs(streamInputs);

        ReflectionTestUtils.setField(streamService, "serverAddress" , "localhost");
        ReflectionTestUtils.setField(streamService, "serverPort" , "8081");

        assertTrue(streamService.startStream(stream));

    }*/
}