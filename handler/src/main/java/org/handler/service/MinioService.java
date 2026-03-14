package org.handler.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioService {
    List<String> uploadPhotos(List<MultipartFile> photos, Long caseId);
}
