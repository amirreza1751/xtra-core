package com.xtra.core.service;

import com.xtra.core.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileSystemService {

    @Autowired
    public FileSystemService() {
    }

    public List<File> list(String path) {
        java.io.File directoryPath = new java.io.File(path);
        if (!directoryPath.exists() || !path.startsWith(System.getProperty("user.home"))) {
            return new ArrayList<>();
        }
        java.io.File[] filesList = directoryPath.listFiles();
        ArrayList<com.xtra.core.model.File> result = new ArrayList<>();
        if (filesList != null) {
            for (java.io.File file : filesList) {
                if (!file.isHidden()) {
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

    public void deleteOldSegments(long afterMilliSeconds, String fileExtension, String dirPath) {

        java.io.File folder = new java.io.File(dirPath);

        if (folder.exists()) {

            java.io.File[] listFiles = folder.listFiles();

            long eligibleForDeletion = System.currentTimeMillis() - afterMilliSeconds;
//                    (days * 24 * 60 * 60 * 1000L);

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
