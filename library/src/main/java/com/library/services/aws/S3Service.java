package com.library.services.aws;

import com.library.services.exceptions.MediaDeleteException;
import com.library.services.exceptions.MediaUploadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.library.services.IS3Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service implements IS3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files,
                                    String folder, Long referenceId) {
        if (files == null || files.isEmpty()) {
            throw new MediaUploadException("No files provided for upload");
        }

        log.info("Uploading {} file(s) | folder={} | referenceId={}",
                files.size(), folder, referenceId);

        List<String> urls = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            validateFile(file);
            String extension = getExtension(file.getOriginalFilename());
            String fileName = referenceId + "_" + (i + 1) + extension;
            urls.add(uploadFile(file, folder, fileName));
        }

        log.info("All files uploaded successfully | folder={} | referenceId={} | count={}",
                folder, referenceId, urls.size());

        return urls;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder, String fileName) {
        validateFile(file);

        String key = folder + "/" + fileName;

        log.info("Uploading file to S3 | bucket={} | key={} | size={}bytes | contentType={}",
                bucketName, key, file.getSize(), file.getContentType());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            String url = getFileUrl(key);
            log.info("File uploaded successfully | key={} | url={}", key, url);
            return url;

        } catch (IOException e) {
            log.error("Upload failed | key={} | error={}", key, e.getMessage());
            throw new MediaUploadException("Error uploading file: " + key);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        log.info("Deleting file from S3 | url={}", fileUrl);

        try {
            URI uri = new URI(fileUrl);
            String key = uri.getPath();

            if (key.startsWith("/")) {
                key = key.substring(1);
            }

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build()
            );

            log.info("File deleted successfully | bucket={} | key={}", bucketName, key);

        } catch (Exception e) {
            log.error("Delete failed | url={} | error={}", fileUrl, e.getMessage());
            throw new MediaDeleteException("Error deleting file");
        }
    }

    private void validateFile(MultipartFile file) {
        validateFileSize(file);
        validateFileType(file);
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File rejected — size exceeded | size={}bytes | max={}bytes",
                    file.getSize(), MAX_FILE_SIZE);
            throw new MediaUploadException(
                    "File size exceeds the maximum allowed of 5MB. Received: "
                            + (file.getSize() / (1024 * 1024)) + "MB");
        }
    }

    private void validateFileType(MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            log.warn("File rejected — invalid type | contentType={}", file.getContentType());
            throw new MediaUploadException(
                    "Invalid file type: " + file.getContentType()
                            + ". Allowed types: JPG, PNG, WebP");
        }
    }

    private String getFileUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}