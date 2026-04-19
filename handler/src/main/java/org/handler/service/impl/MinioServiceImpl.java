package org.handler.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.service.MinioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public List<String> uploadPhotos(List<MultipartFile> photos, Long caseId) {
        List<String> photoObjectNames = new ArrayList<>();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            for (MultipartFile photo : photos) {
                String extension = getFileExtension(photo.getOriginalFilename());
                String objectName = "case-" + caseId + "/" + UUID.randomUUID() + "." + extension;

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(photo.getInputStream(), photo.getSize(), -1)
                                .contentType(photo.getContentType())
                                .build()
                );

                photoObjectNames.add(objectName);
                log.info("Uploaded photo {} for case {}", objectName, caseId);
            }
        } catch (Exception e) {
            log.error("Error uploading photos for case {}", caseId, e);
            throw new RuntimeException("Failed to upload photos", e);
        }

        return photoObjectNames;
    }

    @Override
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(60 * 60)
                            .build()
            );

        } catch (Exception e) {
            log.error("Failed generating presigned URL for {}", objectName, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }

        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}