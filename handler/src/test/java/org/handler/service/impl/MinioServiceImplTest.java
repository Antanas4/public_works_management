package org.handler.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;

    private MinioServiceImpl minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioServiceImpl(minioClient);
        ReflectionTestUtils.setField(minioService, "bucketName", "test-bucket");
    }

    @Test
    void uploadPhotos_ShouldUploadAllPhotos_WhenBucketExists() throws Exception {
        MultipartFile photoOne = mock(MultipartFile.class);
        when(photoOne.getOriginalFilename()).thenReturn("first.jpg");
        when(photoOne.getInputStream()).thenReturn(new ByteArrayInputStream("one".getBytes()));
        when(photoOne.getSize()).thenReturn(3L);
        when(photoOne.getContentType()).thenReturn("image/jpeg");

        MultipartFile photoTwo = mock(MultipartFile.class);
        when(photoTwo.getOriginalFilename()).thenReturn("second.png");
        when(photoTwo.getInputStream()).thenReturn(new ByteArrayInputStream("two".getBytes()));
        when(photoTwo.getSize()).thenReturn(3L);
        when(photoTwo.getContentType()).thenReturn("image/png");

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        List<String> uploaded = minioService.uploadPhotos(List.of(photoOne, photoTwo), 100L);

        assertEquals(2, uploaded.size());
        assertTrue(uploaded.get(0).startsWith("case-100/"));
        assertTrue(uploaded.get(0).endsWith(".jpg"));
        assertTrue(uploaded.get(1).startsWith("case-100/"));
        assertTrue(uploaded.get(1).endsWith(".png"));

        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient, times(2)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadPhotos_ShouldCreateBucketAndUploadPhotos_WhenBucketDoesNotExist() throws Exception {
        MultipartFile photo = mock(MultipartFile.class);
        when(photo.getOriginalFilename()).thenReturn("image.jpeg");
        when(photo.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(photo.getSize()).thenReturn(4L);
        when(photo.getContentType()).thenReturn("image/jpeg");

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        List<String> uploaded = minioService.uploadPhotos(List.of(photo), 200L);

        assertEquals(1, uploaded.size());
        assertTrue(uploaded.getFirst().startsWith("case-200/"));
        assertTrue(uploaded.getFirst().endsWith(".jpeg"));

        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }


    @Test
    void uploadPhotos_ShouldThrowRuntimeException_WhenBucketExistsCheckFails() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new RuntimeException("Minio down"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> minioService.uploadPhotos(List.of(mock(MultipartFile.class)), 400L)
        );

        assertEquals("Failed to upload photos", exception.getMessage());
        assertNotNull(exception.getCause());
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    void getPresignedUrl_ShouldReturnUrl_WhenMinioReturnsSuccessfully() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio.local/test-bucket/case-1/photo.jpg?sig=abc");

        String url = minioService.getPresignedUrl("case-1/photo.jpg");

        assertEquals("https://minio.local/test-bucket/case-1/photo.jpg?sig=abc", url);

        ArgumentCaptor<GetPresignedObjectUrlArgs> argsCaptor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(argsCaptor.capture());
        assertNotNull(argsCaptor.getValue());
    }

}
