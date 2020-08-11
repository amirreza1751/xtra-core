package com.xtra.core.controller;
import com.xtra.core.repository.LineActivityRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.service.LineService;
import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;


@RestController
public class FileSystemController {


    @Autowired
    public FileSystemController() {}

    @GetMapping("list")
    public void list(@RequestParam String path){
        //Creating a File object for directory
        File directoryPath = new File(path);
        //List of all files and directories
        File[] filesList = directoryPath.listFiles();
        System.out.println("List of files and directories in the specified directory:");
        for(File file : filesList) {
            System.out.println("File name: "+file.getName());
            System.out.println("File path: "+file.getAbsolutePath());
            System.out.println("Size :"+file.getTotalSpace());
            System.out.println(" ");
        }
    }


}