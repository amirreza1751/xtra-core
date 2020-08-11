package com.xtra.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@RestController
public class FileSystemController {


    @Autowired
    public FileSystemController() {}

    @GetMapping("list")
    public List<com.xtra.core.model.File> list(@RequestParam String path){
        File directoryPath = new File(path);
        if (!directoryPath.exists()) {return new ArrayList<com.xtra.core.model.File>();}
        File[] filesList = directoryPath.listFiles();
        ArrayList<com.xtra.core.model.File> result = new ArrayList<>();
        for (File file : filesList) {
                result.add(new com.xtra.core.model.File(file.getName(), file.getAbsolutePath(), (file.isDirectory()) ? file.getTotalSpace() : file.length(), file.isDirectory()));
            }

            return result;

    }


}