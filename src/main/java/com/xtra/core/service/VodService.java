package com.xtra.core.service;

import com.xtra.core.model.Audio;
import com.xtra.core.model.Subtitle;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.mozilla.universalchardet.UniversalDetector;

@Service
public class VodService {

    private final ProcessService processService;

    public VodService(ProcessService processService) {
        this.processService = processService;
    }

    public String encode(String video_path){
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

    public String add_subtitle(String video_path, List<Subtitle> subtitles) throws IOException {
        Path path = Paths.get(video_path);
        String file_directory = path.getParent().toString();
        String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
        String output_video = file_directory + "/" + file_name_without_extension + "_sub.mp4";

        subtitles.removeIf(subtitle -> {
            try {
                return this.get_file_encoding(subtitle).equals("unknown");
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
            encoding = this.get_file_encoding(subtitles.get(i));
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

    public String get_file_encoding(Subtitle subtitle) throws IOException {
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

    public String add_audio(String video_path, List<Audio> audios){
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
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return "Add subtitles failed.";
        }
        return output_video;
    }

}
