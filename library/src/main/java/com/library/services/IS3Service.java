package com.library.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interface defining the S3 storage operations for file upload and deletion.
 */
public interface IS3Service {

    /**
     * Uploads multiple files to S3 under the same folder and reference ID.
     * Each file is validated for size and type before upload.
     * Files are named as {referenceId}_{index}.{extension}.
     *
     * @param files       list of files to upload
     * @param folder      S3 folder path (e.g. "books", "users")
     * @param referenceId ID used to group files (e.g. bookId, userId)
     * @return list of public URLs of the uploaded files
     * @throws MediaUploadException if no files are provided, size exceeds 5MB,
     *                              or file type is not JPG, PNG or WebP
     */
    List<String> uploadFiles(List<MultipartFile> files, String folder, Long referenceId);

    /**
     * Uploads a single file to S3 under the specified folder and file name.
     * The file is validated for size and type before upload.
     *
     * @param file     the file to upload
     * @param folder   S3 folder path (e.g. "books", "users")
     * @param fileName the name to use for the file in S3
     * @return public URL of the uploaded file
     * @throws MediaUploadException if size exceeds 5MB or file type is not allowed
     */
    String uploadFile(MultipartFile file, String folder, String fileName);

    /**
     * Deletes a file from S3 using its public URL.
     * Extracts the S3 key from the URL path automatically.
     *
     * @param fileUrl the public URL of the file to delete
     * @throws MediaDeleteException if the file cannot be deleted
     */
    void deleteFile(String fileUrl);
}