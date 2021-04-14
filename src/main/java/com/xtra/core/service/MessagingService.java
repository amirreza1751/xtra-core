package com.xtra.core.service;

import com.xtra.core.projection.StreamDetailsView;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagingService {

    private final RabbitTemplate template;

    @Qualifier("streamStatusQueue")
    private final Queue streamStatusQueue;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    public MessagingService(RabbitTemplate template, Queue streamStatusQueue) {
        this.template = template;
        this.streamStatusQueue = streamStatusQueue;
    }

    public void sendStreamStatus(List<StreamDetailsView> statuses) {
        template.convertAndSend(streamStatusQueue.getName(), statuses, message -> {
            message.getMessageProperties().getHeaders().put("server_address", "127.0.0.1");
            message.getMessageProperties().getHeaders().put("server_port", serverPort);
            return message;
        });
    }
}
