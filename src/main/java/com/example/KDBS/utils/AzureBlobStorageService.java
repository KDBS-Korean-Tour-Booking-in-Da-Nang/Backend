package com.example.KDBS.utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class AzureBlobStorageService implements FileStorageService {

    @Value("${azure.blob.connection-string}")
    private String connectionString;

    @Value("${azure.blob.container-name}")
    private String containerName;

    @Override
    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        String normalizedSubDir = (subDir == null) ? "" : subDir.trim();
        if (normalizedSubDir.startsWith("/")) {
            normalizedSubDir = normalizedSubDir.substring(1);
        }
        if (normalizedSubDir.endsWith("/")) {
            normalizedSubDir = normalizedSubDir.substring(0, normalizedSubDir.length() - 1);
        }

        // Sanitize filename
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String fileName = System.currentTimeMillis() + "_" + sanitizedFilename;

        String blobPath = normalizedSubDir.isEmpty() ? fileName : normalizedSubDir + "/" + fileName;

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
        }

        BlobClient blobClient = containerClient.getBlobClient(blobPath);

        // Detect PDF
        boolean isPdf = "application/pdf".equalsIgnoreCase(file.getContentType())
                || sanitizedFilename.toLowerCase().endsWith(".pdf");

        // Set content type
        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(isPdf ? "application/pdf" : file.getContentType());

        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);

        // Generate SAS URL with long expiry for avatars
        return generateSasUrl(blobClient);
    }

    private String generateSasUrl(BlobClient blobClient) {
        OffsetDateTime expiryTime = OffsetDateTime.now().plusYears(10);

        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission);

        String sasToken = blobClient.generateSas(sasValues);

        return blobClient.getBlobUrl() + "?" + sasToken;
    }
}

