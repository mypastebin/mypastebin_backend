package com.hhnatsiuk.mypastebin_backend.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.hhnatsiuk.mypastebin_backend.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

@Service
public class GoogleCloudStorageService {

    private static final Logger logger = LogManager.getLogger(GoogleCloudStorageService.class);

    private final Storage storage;

    @Autowired
    public GoogleCloudStorageService(Storage storage) {
        this.storage = storage;
    }

    private final String bucketName = "mypastebinbucket";

    public String uploadFile(byte[] content, String contentType, String fileName, Post post) {
        if (content == null) {
            logger.error("File content is null for file: {}", fileName);
            throw new IllegalArgumentException("Content cannot be null");
        }

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        storage.create(blobInfo, content);

        Blob blob = storage.get(blobId);
        String mediaLink = blob.getMediaLink();
        logger.debug("mediaLink of {} = {}", fileName, mediaLink);

        long fileSize = (long) content.length;
        post.setFileSize(fileSize);
        logger.info("File size of {} = {} bytes", fileName, fileSize);

        return mediaLink;
    }

    public void deleteFile(String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        boolean deleted = storage.delete(blobId);
        if (deleted) {
            logger.info("File {} successfully deleted from bucket {}", fileName, bucketName);
        } else {
            logger.warn("File {} not found in bucket {}", fileName, bucketName);
        }
    }

    public String downloadFile(String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        Blob blob = storage.get(blobId);

        if (blob != null && blob.exists()) {
            byte[] content = blob.getContent();
            String contentString = new String(content, StandardCharsets.UTF_8);
            logger.debug("Downloaded content from file {}: {}", fileName, contentString);
            return contentString;
        } else {
            logger.warn("File {} not found in bucket {}", fileName, bucketName);
            return null;
        }
    }
}
