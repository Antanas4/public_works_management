package org.handler.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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

    @Value("${minio.endpoint}")
    private String minioUrl;

    @Override
    public List<String> uploadPhotos(List<MultipartFile> photos, Long caseId) {
        List<String> photoUrls = new ArrayList<>();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            for (MultipartFile photo : photos) {
                String fileName = "case-" + caseId + "-" + UUID.randomUUID() + photo.getOriginalFilename();
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(photo.getInputStream(), photo.getSize(), -1)
                        .contentType(photo.getContentType())
                        .build()
                );
                String photoUrl = minioUrl + "/" + bucketName + "/" + fileName;
                photoUrls.add(photoUrl);
                log.info("Uploaded photo {} for case {}", fileName, caseId);
            }
        } catch (Exception e) {
            log.error("Error uploading photos for case {}", caseId, e);
            throw new RuntimeException("Failed to upload photos", e);
        }

        return photoUrls;
    }
}
