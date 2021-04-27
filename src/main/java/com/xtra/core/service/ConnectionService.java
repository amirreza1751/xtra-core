package com.xtra.core.service;

import com.xtra.core.model.Connection;
import com.xtra.core.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConnectionService {
    private final ConnectionRepository connectionRepository;

    @Autowired
    public ConnectionService(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    public boolean updateConnection(String lineToken, String streamToken, String ipAddress, String userAgent) {
        Optional<Connection> existingConnection = connectionRepository.findByLineTokenAndStreamTokenAndUserIp(lineToken, streamToken, ipAddress);
        Connection connection;
        if (existingConnection.isPresent()) {
            if (existingConnection.get().isHlsEnded()) {
                return false;
            }
            connection = existingConnection.get();
        } else {
            connection = new Connection(lineToken, streamToken, ipAddress);
        }
        connection.setUserAgent(userAgent);
        connection.setLastRead(LocalDateTime.now());
        connectionRepository.save(connection);
        return true;
    }

}
