package com.xtra.core.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Options {
    private Map<String, String> inputKeyValues;
    private Map<String, String> outputKeyValues;
    private Set<String> inputFlags;
    private Set<String> outputFlags;
}
