package com.xtra.core.service;

import com.xtra.core.model.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileSystemService {

    @Autowired
    public FileSystemService() {
    }

    @Value("${vod.path.prefix}")
    private String vodPathPrefix;

    public List<File> list(String path) {
        java.io.File directoryPath = new java.io.File(vodPathPrefix + java.io.File.separator + path);
        try {
            if (!directoryPath.exists() || !directoryPath.getCanonicalPath().startsWith(vodPathPrefix)) {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        java.io.File[] filesList = directoryPath.listFiles();
        ArrayList<com.xtra.core.model.File> result = new ArrayList<>();
        if (filesList != null) {
            for (java.io.File file : filesList) {
                if (!file.isHidden()) {
                    result.add(
                            new File(
                                    file.getName(),
                                    file.getAbsolutePath(),
                                    (file.isDirectory()) ? FileUtils.sizeOfDirectory(file) : file.length(),
                                    file.isDirectory())
                    );
                }
            }
        }
        return result;
    }

    public void deleteOldFilesAfterDays(long days, String fileExtension, String dirPath) {

        java.io.File folder = new java.io.File(dirPath);

        if (folder.exists()) {

            java.io.File[] listFiles = folder.listFiles();

            long eligibleForDeletion = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);

            for (java.io.File listFile : listFiles) {
                if (listFile.getName().endsWith(fileExtension) &&
                        listFile.lastModified() < eligibleForDeletion) {

                    if (!listFile.delete()) {

                        System.out.println("Sorry Unable to Delete Files..");

                    }
                }
            }
        }
    }
}
