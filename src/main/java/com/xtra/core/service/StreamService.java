package com.xtra.core.service;

import com.xtra.core.model.Stream;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class StreamService {
    public long StartStream(Stream stream){

        File liveStreamsDirectory = new File(
                System.getProperty("user.home") + File.separator +
                        "xtra-project" + File.separator +
                        "xtra-core" + File.separator +
                        "live-streams"
        );
        if (!liveStreamsDirectory.exists()){
            liveStreamsDirectory.mkdirs();
        }

        String[] args = new String[] {
                "ffmpeg",
                "-re",
                "-i",
//                "http://goiptv.co:8080/live:cross2hosting/cIfILzx9ji/3994",
                stream.getCurrentInput().getUrl(),
                "-vcodec",
                "copy",
                "-loop",
                "-1",
                "-c:a",
                "aac",
                "-b:a",
                "160k",
                "-ar",
                "44100",
                "-strict",
                "-2",
                "-f",
                "hls",
                "-segment_format",
                "mpegts",
                "-segment_time",
                "10",
                "-segment_list_size",
                "6",
                "-segment_format_options",
                "mpegts_flags=+initial_discontinuity:mpegts_copyts=1",
                "-segment_list_type",
                "m3u8",
                "-hls_flags",
                "delete_segments",
                "-segment_list",
                liveStreamsDirectory.getAbsolutePath() + "/" + stream.getId() + "_%d.ts",
                liveStreamsDirectory.getAbsolutePath()  + "/" + stream.getId()+ "_.m3u8"
        };
//        ffmpeg -re -i http://goiptv.co:8080/live:cross2hosting/cIfILzx9ji/3994
//        -vcodec copy -loop -1 -c:a aac -b:a 160k -ar 44100 -strict -2
//        -f hls -segment_format mpegts -segment_time 10 -segment_list_size 6
//        -segment_format_options mpegts_flags=+initial_discontinuity:mpegts_copyts=1
//        -segment_list_type m3u8 -hls_flags delete_segments -segment_list
//        /home/amirak/xtreamcodes/iptv_xtream_codes/streams/57852_%d.ts
//        /home/amirak/xtreamcodes/iptv_xtream_codes/streams/57852_.m3u8 ";
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return proc.pid();
    }
}
