package com.xtra.core.model;

import lombok.Data;


@Data
public class Resource {
    private Long id;

    private String name;
    private String total;
    private String used;

}
