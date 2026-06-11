package com.library.services.aws;

import com.library.services.exceptions.MediaDeleteException;
import com.library.services.exceptions.MediaUploadException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    private S3Service service;

    private MultipartFile validFile;

    @BeforeEach
    void setUp() {
        service = new S3Service(s3Client);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
        validFile = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "image-content".getBytes());
    }

    @Test
    void uploadFileShouldReturnUrlWhenValid() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String url = service.uploadFile(validFile, "books", "1_1.jpg");

        assertNotNull(url);
        assertTrue(url.contains("test-bucket"));
        assertTrue(url.contains("books/1_1.jpg"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFileShouldThrowMediaUploadExceptionWhenTypeInvalid() {
        MultipartFile invalid = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "data".getBytes());

        assertThrows(MediaUploadException.class,
                () -> service.uploadFile(invalid, "books", "1.pdf"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFileShouldThrowMediaUploadExceptionWhenSizeExceeded() {
        byte[] bigContent = new byte[6 * 1024 * 1024]; // 6MB
        MultipartFile big = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", bigContent);

        assertThrows(MediaUploadException.class,
                () -> service.uploadFile(big, "books", "big.jpg"));
    }

    @Test
    void uploadFilesShouldReturnUrlsWhenValid() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        MultipartFile file2 = new MockMultipartFile(
                "file", "cover2.png", "image/png", "content".getBytes());

        List<String> urls = service.uploadFiles(List.of(validFile, file2), "books", 1L);

        assertEquals(2, urls.size());
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFilesShouldThrowMediaUploadExceptionWhenListIsNull() {
        assertThrows(MediaUploadException.class,
                () -> service.uploadFiles(null, "books", 1L));
    }

    @Test
    void uploadFilesShouldThrowMediaUploadExceptionWhenListIsEmpty() {
        assertThrows(MediaUploadException.class,
                () -> service.uploadFiles(List.of(), "books", 1L));
    }

    @Test
    void deleteFileShouldCallS3ClientWhenValidUrl() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(null);

        String url = "https://test-bucket.s3.amazonaws.com/books/1_1.jpg";

        assertDoesNotThrow(() -> service.deleteFile(url));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFileShouldThrowMediaDeleteExceptionWhenS3Fails() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("fail").build());

        String url = "https://test-bucket.s3.amazonaws.com/books/1_1.jpg";

        assertThrows(MediaDeleteException.class, () -> service.deleteFile(url));
    }
}
