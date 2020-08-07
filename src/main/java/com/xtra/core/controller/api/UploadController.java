package com.xtra.core.controller.api;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @PostMapping("")
    public boolean uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("path") String path) {
        System.out.println(path);
        return true;
    }
}
