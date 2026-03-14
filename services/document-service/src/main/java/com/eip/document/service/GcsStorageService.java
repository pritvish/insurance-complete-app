package com.eip.document.service;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GcsStorageService {

    private final Storage storage;

    @Value("${document.gcs.bucket-name}")
    private String bucketName;

    public String uploadDocument(String objectKey, byte[] content, String contentType) {
        BlobId blobId = BlobId.of(bucketName, objectKey);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        Blob blob = storage.create(blobInfo, content);
        log.info("Uploaded document to GCS: gs://{}/{} ({} bytes)", bucketName, objectKey, content.length);
        return objectKey;
    }

    public String generateSignedUrl(String objectKey, int expirationMinutes) {
        BlobId blobId = BlobId.of(bucketName, objectKey);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        URL signedUrl = storage.signUrl(blobInfo, expirationMinutes, TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature());
        return signedUrl.toString();
    }

    public byte[] downloadDocument(String objectKey) {
        Blob blob = storage.get(BlobId.of(bucketName, objectKey));
        if (blob == null) {
            throw new RuntimeException("Document not found in GCS: " + objectKey);
        }
        return blob.getContent();
    }

    public String getBucketName() {
        return bucketName;
    }
}
