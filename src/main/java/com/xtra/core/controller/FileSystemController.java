package com.xtra.core.controller;

import com.xtra.core.service.FileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/file")
public class FileSystemController {
    FileSystemService fileSystemService;

    @Autowired
    public FileSystemController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @GetMapping("/list_files")
    public List<com.xtra.core.model.File> list(@RequestParam String path) {
        return fileSystemService.list(path);
    }


}