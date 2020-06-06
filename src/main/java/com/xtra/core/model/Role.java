package com.xtra.core.model;

import lombok.Data;


@Data
public class Role {
    private long id;
    private String name;
    private String color;
    private UserType userType;
}
