package com.xtra.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/file")
public class FileSystemController {


    @Autowired
    public FileSystemController() {
    }

    @GetMapping("/list_files")
    public List<com.xtra.core.model.File> list(@RequestParam String path) {
        File directoryPath = new File(path);
        if (!directoryPath.exists() || !path.startsWith(System.getProperty("user.home"))) {
            return new ArrayList<>();
        }
        File[] filesList = directoryPath.listFiles();
        ArrayList<com.xtra.core.model.File> result = new ArrayList<>();
        if (filesList != null) {
            for (File file : filesList) {
                if (!file.isHidden()){
                    result.add(
                            new com.xtra.core.model.File(
                                    file.getName(),
                                    file.getAbsolutePath(),
                                    (file.isDirectory()) ? file.getTotalSpace() : file.length(),
                                    file.isDirectory())
                    );
                }
            }
        }

        return result;
    }


}