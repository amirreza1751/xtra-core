package com.xtra.core.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
public class VodService {

    private final ProcessService processService;

    public VodService(ProcessService processService) {
        this.processService = processService;
    }

    public String encode(String input_path){
         Path path = Paths.get(input_path);
         String file_directory = path.getParent().toString();
         String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
         String output_video = file_directory + file_name_without_extension + "_encoded.mp4";
         String[] args = new String[]{
                 "ffmpeg",
                 "-i",
                 input_path,
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
}
