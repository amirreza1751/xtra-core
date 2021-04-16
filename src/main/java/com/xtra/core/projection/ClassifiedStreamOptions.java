package com.xtra.core.projection;

import lombok.Data;

@Data
public class ClassifiedStreamOptions {
    private String[] inputKeyValues;
    private String[] inputFlags;
    private String[] outputKeyValues;
    private String[] outputFlags;
}
