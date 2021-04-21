package com.xtra.core.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Configuration {
    @Id
    @Column(name = "\"key\"")
    private String key;
    private String value;
}
