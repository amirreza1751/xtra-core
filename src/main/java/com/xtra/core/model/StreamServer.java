package com.xtra.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Getter
@Setter
public class StreamServer {
    @EmbeddedId
    private StreamServerId id;
    private Stream stream;
    private Server server;
    private StreamInfo streamInfo;
    private ProgressInfo progressInfo;
    private int selectedSource;

    public StreamServer(StreamServerId id) {
        this.id = id;
    }

    public StreamServer() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof StreamServer))
            return false;
        StreamServer streamServer = (StreamServer) obj;
        return streamServer.id.getServerId().equals(this.id.getServerId()) && streamServer.id.getStreamId().equals(this.id.getStreamId());
    }
}
