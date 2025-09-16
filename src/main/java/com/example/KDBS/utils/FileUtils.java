package com.example.KDBS.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    // Supported image MIME types
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "image/webp", "image/svg+xml", "image/bmp", "image/tiff"
    };

    public static String convertFileToPath(MultipartFile file, String uploadDir, String subDir) throws IOException {
        // Validate file type for images
        if (subDir != null && (subDir.contains("thumbnails") || subDir.contains("images"))) {
            validateImageFile(file);
        }

        // Normalize subDir for filesystem and for returned web path
        String normalizedSubDir = subDir == null ? "" : subDir.trim();
        // Web path must start with a single leading slash
        String webSubDir = normalizedSubDir.startsWith("/") ? normalizedSubDir
                : (normalizedSubDir.isEmpty() ? "" : "/" + normalizedSubDir);
        // Filesystem path must NOT start with a leading slash
        String fsSubDir = webSubDir.startsWith("/") ? webSubDir.substring(1) : webSubDir;

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = fsSubDir.isEmpty() ? Paths.get(uploadDir) : Paths.get(uploadDir, fsSubDir);

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }

        // Save the file
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath);

        // Return the relative path for frontend access (always "/uploads/.../")
        return "/uploads" + (webSubDir.isEmpty() ? "" : webSubDir) + "/" + fileName;
    }

    private static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("File type cannot be determined");
        }

        boolean isValidType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException("Unsupported image format: " + contentType +
                    ". Supported formats: JPG, PNG, GIF, WebP, SVG, BMP, TIFF");
        }

        // Validate file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }
}
