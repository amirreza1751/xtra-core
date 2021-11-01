package com.xtra.core.dto;

import lombok.Getter;

@Getter
public enum VideoCodec {
    H264("h264", "libx264"),
    H265("h265", "libx265"),
    VP8("vp8", "libvpx"),
    VP9("vp9", "libvpx-vp9"),
    AV1("av1", "libaom-av1");

    private final String text;
    private final String codec;

    VideoCodec(String text, String codec) {
        this.text = text;
        this.codec = codec;
    }
    public static VideoCodec findByText(String text){
        for(VideoCodec v : values()){
            if( v.text.equals(text)){
                return v;
            }
        }
        return null;
    }
}