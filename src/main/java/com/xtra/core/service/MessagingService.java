package com.xtra.core.service;

import com.xtra.core.config.DynamicConfig;
import com.xtra.core.projection.ConnectionDetails;
import com.xtra.core.projection.StreamDetailsView;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagingService {

    private final RabbitTemplate template;

    @Qualifier("streamStatusQueue")
    private final Queue streamStatusQueue;

    private final DynamicConfig config;

    @Autowired
    public MessagingService(RabbitTemplate template, Queue streamStatusQueue, DynamicConfig config) {
        this.template = template;
        this.streamStatusQueue = streamStatusQueue;
        this.config = config;
    }

    public void sendStreamStatus(List<StreamDetailsView> statuses) {
        template.convertAndSend(streamStatusQueue.getName(), statuses, message -> {
            message.getMessageProperties().getHeaders().put("token", config.getServerToken());
            return message;
        });
    }

}
