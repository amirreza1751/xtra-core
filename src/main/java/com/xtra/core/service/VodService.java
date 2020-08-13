package com.xtra.core.service;

import com.xtra.core.model.Audio;
import com.xtra.core.model.Subtitle;
import com.xtra.core.model.Vod;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class VodService {

    @Value("${main.apiPath}")
    private String mainApiPath;


    public String encode(Vod vod){
         String video_path = vod.getLocation();
         Path path = Paths.get(video_path);
         String file_directory = path.getParent().toString();
         String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
         String output_video = file_directory + "/" + file_name_without_extension + "_encoded.mp4";
         String[] args = new String[]{
                 "ffmpeg",
                 "-i",
                 video_path,
                 "-vcodec",
                 "libx264",
                 "-acodec",
                 "aac",
                 output_video,
         };
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return "Encode failed.";
        }

         return output_video;
    }

    public String setSubtitles(Vod vod) throws IOException {
        String video_path = vod.getLocation();
        List<Subtitle> subtitles = vod.getSubtitles();
        Path path = Paths.get(video_path);
        String file_directory = path.getParent().toString();
        String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
        String output_video = file_directory + "/" + file_name_without_extension + "_sub.mp4";

        subtitles.removeIf(subtitle -> {
            try {
                return this.getFileEncoding(subtitle).equals("unknown");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });
        ArrayList<String> args = new ArrayList<>(Arrays.asList(
                "ffmpeg",
                "-i",
                video_path,
                "-c:v",
                "copy",
                "-c:a",
                "copy",
                "-c:s",
                "mov_text",
                output_video,
                "-y"
                ));
        ArrayList<String> sub_info = new ArrayList<>();
        ArrayList<String> map_option = new ArrayList<>();
        ArrayList<String> sub_label = new ArrayList<>();
//        System.out.println("final subs = " + subtitles.toString());
        String encoding;
        for (int i = 0; i < subtitles.size(); i++){
            encoding = this.getFileEncoding(subtitles.get(i));
//            System.out.println("i = " + i);
            sub_info.addAll(Arrays.asList("-sub_charenc", "\""+encoding+"\"", "-i", subtitles.get(i).getLocation()));
            map_option.addAll(Arrays.asList("-map", Integer.toString(i)));
            sub_label.addAll(Arrays.asList("-metadata:s:s:"+ i, "language="+subtitles.get(i).getLanguage()));

        }
        map_option.addAll(Arrays.asList("-map", Integer.toString(subtitles.size())));
        args.addAll(args.indexOf("-c:v"), sub_info);
        args.addAll(args.indexOf("-c:v"), map_option);
        args.addAll(args.indexOf(output_video), sub_label);
         Process proc;
         try {
             proc = new ProcessBuilder(args).start();
             proc.waitFor();
         } catch (IOException | InterruptedException e) {
             return "Add subtitles failed.";
         }
         return output_video;
    }

    public String getFileEncoding(Subtitle subtitle) throws IOException {
        UniversalDetector detector = new UniversalDetector(null);
        FileInputStream fis;
        byte[] buf = new byte[4096];
        int nread;
        String encoding = "";
        fis = new FileInputStream(subtitle.getLocation());
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding != null) {
//            System.out.println("Detected encoding = " + encoding);
            return encoding;
        } else {
//            System.out.println("No encoding detected.");
            return "unknown";
        }

    }

    public String setAudios(Vod vod){
        String video_path = vod.getLocation();
        List<Audio> audios = vod.getAudios();
        Path path = Paths.get(video_path);
        String file_directory = path.getParent().toString();
        String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
        String output_video = file_directory + "/" + file_name_without_extension + "_audio_added.mp4";
        ArrayList<String> args = new ArrayList<>(Arrays.asList(
                "ffmpeg",
                "-i",
                video_path,
                //input audios
                "-c",
                "copy",
                "-c:s",
                "mov_text",
                // map option
                "mov_text",
                output_video,
                "-y"
        ));

        ArrayList<String> audio_info = new ArrayList<>();
        ArrayList<String> map_option = new ArrayList<>();
        for (int i = 0; i < audios.size(); i++){
            audio_info.addAll(Arrays.asList("-i", audios.get(i).getLocation()));
            map_option.addAll(Arrays.asList("-map", Integer.toString(i)));
        }
        map_option.addAll(Arrays.asList("-map", Integer.toString(audios.size())));
        args.addAll(args.indexOf("-c"), audio_info);
        args.addAll(args.indexOf("mov_text"), map_option);

        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return "Add audios failed.";
        }
        return output_video;
    }

    public String getVodLocation(String streamId) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return Objects.requireNonNull(restTemplate.getForObject(mainApiPath + "/vod/" + streamId, Vod.class)).getLocation();
        } catch (HttpClientErrorException exception) {
            System.out.println(exception.getMessage());
            return "";
        }
    }

}
