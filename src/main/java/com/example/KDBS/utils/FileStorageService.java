package com.example.KDBS.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface FileStorageService {
    String uploadFile(MultipartFile file, String subDir) throws IOException;
}
