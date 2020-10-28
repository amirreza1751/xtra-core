package com.xtra.core.model;

import lombok.Data;

import javax.persistence.*;


@Data
public class StreamServer {
    @EmbeddedId
    private StreamServerId id;
    private Stream stream;
    private Server server;
    private StreamInfo streamInfo;
    private ProgressInfo progressInfo;

    public StreamServer(StreamServerId id) {
        this.id = id;
    }

    public StreamServer() {
    }

}
