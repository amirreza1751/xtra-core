package com.xtra.core.model;

import lombok.Data;


@Data
public class Process {
    private long id;
    private long pid;

    private Stream stream;
}
