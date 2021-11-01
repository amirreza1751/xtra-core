package com.xtra.core.dto;

import lombok.Getter;

@Getter
public enum AudioCodec {
    AAC("aac", "aac"),
    MP3("mp3", "libmp3lame"),
    AC3("ac3", "ac3"),
    EAC3("eac3", "eac3"),
    VORBIS("vorbis", "libvorbis"),
    OPUS("opus", "libopus"),
    FLAC("flac", "flac"),
    DTS("dts", "dts");

    private final String text;
    private final String codec;

    AudioCodec(String text, String codec) {
        this.text = text;
        this.codec = codec;
    }
    public static AudioCodec findByText(String text){
        for(AudioCodec a : values()){
            if( a.text.equals(text)){
                return a;
            }
        }
        return null;
    }
}
