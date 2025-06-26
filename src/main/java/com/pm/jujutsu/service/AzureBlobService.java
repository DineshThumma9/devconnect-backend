package com.pm.jujutsu.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AzureBlobService {


    @Autowired
    private final BlobContainerClient blobContainerClient;

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        return blobClient.getBlobUrl();
    }



    public byte[] downloadFile(String fileName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        return outputStream.toByteArray();
    }

    public void deleteFile(String fileName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        blobClient.deleteIfExists();
    }

    public List<String> listFiles() {
        List<String> fileNames = new ArrayList<>();
        blobContainerClient.listBlobs().forEach(blob -> fileNames.add(blob.getName()));
        return fileNames;
    }

    /**
     * Get the base URL of the blob container
     * @return The container URL
     */
    public String getBlobContainerUrl() {
        return blobContainerClient.getBlobContainerUrl();
    }
}
