package com.xtra.core.model;

import lombok.Data;


@Data
public class File {
    private long id;
    private String name;
    private String path;
    private long size;

}
