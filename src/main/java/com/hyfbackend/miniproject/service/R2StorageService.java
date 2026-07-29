package com.hyfbackend.miniproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class R2StorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    public R2StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build(), RequestBody.fromBytes(file.getBytes()));
        return fileName;
    }

    public void deleteFile(String fileKey) {
        if (fileKey != null && !fileKey.isBlank()) {
            s3Client.deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(fileKey)
                    .build());
        }
    }
}
