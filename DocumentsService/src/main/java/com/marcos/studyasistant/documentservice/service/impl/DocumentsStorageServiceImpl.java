package com.marcos.studyasistant.documentservice.service.impl;

import com.marcos.studyasistant.documentservice.config.MinioConfig;
import com.marcos.studyasistant.documentservice.service.DocumentsStorageService;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;
import static com.google.common.io.Files.getFileExtension;

@Service
public class DocumentsStorageServiceImpl implements DocumentsStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public DocumentsStorageServiceImpl(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    @PostConstruct
    public void init() {
        createBucketIfNotExists();
    }

    @Override
    public String uploadDocument(MultipartFile file) throws Exception {
        String filename = generateUniqueFilename(file.getOriginalFilename());
        minioClient.putObject(
                PutObjectArgs.builder()
                .bucket(minioConfig.getMinioBucketName())
                .object(filename)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
        return filename;
    }

    @Override
    public InputStream downloadDocument(String filename) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getMinioBucketName())
                        .object(filename)
                        .build()
        );
    }

    @Override
    public void deleteDocument(String filename) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioConfig.getMinioBucketName())
                        .object(filename)
                        .build()
        );
    }

    @Override
    public void createBucketIfNotExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getMinioBucketName())
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getMinioBucketName())
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating MinIO bucket", e);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID() +
                (extension.isEmpty() ? "" : "." + extension);
    }
}
