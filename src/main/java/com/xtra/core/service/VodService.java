package com.xtra.core.service;

import com.xtra.core.model.Subtitle;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
         String output_video = file_directory + file_name_without_extension + "_encoded.mp4";
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
        StringBuilder sub_info = new StringBuilder();
        StringBuilder map_option = new StringBuilder();
        StringBuilder sub_label = new StringBuilder();
        String encoding = "";
        for (int i=0; i < subtitles.size(); i++){
            encoding = this.get_file_encoding(subtitles.get(i));
            if (encoding.equals("unknown")) continue;
            sub_info.append(" -sub_charenc \"").append(encoding).append("\"").append(" -i ").append(subtitles.get(i).getLocation()).append(" ");
            map_option.append(" -map ").append(i).append(" ");
            sub_label.append(" -metadata:s:s:").append(i).append(" language=").append(subtitles.get(i).getLanguage()).append(" ");
        }
        map_option.append(" -map ").append(subtitles.size()).append(" "); // last map option needs to be added manually

         Path path = Paths.get(video_path);
         String file_directory = path.getParent().toString();
         String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
         String output_video = file_directory + file_name_without_extension + "_sub.mp4";
         String[] args = new String[]{
                 "ffmpeg",
                 "-i",
                 video_path,
                 sub_info.toString(),
                 map_option.toString(),
                 sub_label.toString(),
                 output_video,
         };
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
}
