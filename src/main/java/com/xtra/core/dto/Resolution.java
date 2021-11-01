package com.xtra.core.dto;

import lombok.Getter;

@Getter
public enum Resolution {
    RES_480P("480p", "852"),
    RES_576P("576p", "720"),
    RES_720P("720p", "1280"),
    RES_1080P("1080p", "1920"),
    RES_4K_UHD("4k UHD", "3840");

    private final String text;
    private final String width;

    Resolution(String text, String width) {
        this.text = text;
        this.width = width;
    }
    public static Resolution findByWidth(String text){
        for(Resolution r : values()){
            if( r.width.equals(text)){
                return r;
            }
        }
        return null;
    }
}