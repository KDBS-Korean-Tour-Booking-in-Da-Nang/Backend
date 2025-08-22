package com.example.KDBS.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static String convertFileToPath(MultipartFile file, String uploadDir, String subDir) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir, subDir);

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }

        // Save the file
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath);

        // Return the relative path for frontend access
        return "/uploads" + subDir + "/" + fileName;
    }
}
