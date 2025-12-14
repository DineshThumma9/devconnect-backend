package com.pm.jujutsu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.jujutsu.config.SupabaseConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    @Autowired
    private final OkHttpClient httpClient;

    @Autowired
    private final SupabaseConfig supabaseConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Upload a file to Supabase Storage
     * @param file The file to upload
     * @return The public URL of the uploaded file
     * @throws IOException if upload fails
     */
    public String uploadFile(MultipartFile file,String folder) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String path = folder + "/" + fileName;
        
        String url = String.format("%s/storage/v1/object/%s/%s",
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getBucketName(),
                path);

        RequestBody requestBody = RequestBody.create(
                file.getBytes(),
                MediaType.parse(file.getContentType())
        );

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                .addHeader("Content-Type", file.getContentType())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to upload file: " + response.body().string());
            }
            
            // Return the public URL with full path (folder/filename)
            return getPublicUrl(path);
        }
    }



    public List<String> uploadMultipleFiles(List<MultipartFile> files, String folder) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileUrl = uploadFile(file, folder);
            fileUrls.add(fileUrl);
        }
        return fileUrls;
    }

    /**
     * Download a file from Supabase Storage
     * @param fileName The name of the file to download
     * @return The file content as byte array
     * @throws IOException if download fails
     */
    public byte[] downloadFile(String fileName) throws IOException {
        String url = String.format("%s/storage/v1/object/%s/%s",
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getBucketName(),
                fileName);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file: " + response.body().string());
            }
            return response.body().bytes();
        }
    }

    /**
     * Delete a file from Supabase Storage
     * @param fileName The name of the file to delete
     * @throws IOException if deletion fails
     */
    public void deleteFile(String fileName) throws IOException {
        String url = String.format("%s/storage/v1/object/%s/%s",
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getBucketName(),
                fileName);

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() && response.code() != 404) {
                throw new IOException("Failed to delete file: " + response.body().string());
            }
        }
    }

    /**
     * List all files in the bucket
     * @return List of file names
     * @throws IOException if listing fails
     */
    public List<String> listFiles() throws IOException {
        String url = String.format("%s/storage/v1/object/list/%s",
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getBucketName());

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("{}", MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to list files: " + response.body().string());
            }

            String responseBody = response.body().string();
            List<Map<String, Object>> files = objectMapper.readValue(responseBody, List.class);
            
            List<String> fileNames = new ArrayList<>();
            for (Map<String, Object> file : files) {
                fileNames.add((String) file.get("name"));
            }
            return fileNames;
        }
    }

    /**
     * Get the public URL of a file
     * @param fileName The name of the file
     * @return The public URL
     */
    public String getPublicUrl(String fileName) {
        return String.format("%s/storage/v1/object/public/%s/%s",
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getBucketName(),
                fileName);
    }

    /**
     * Get the base URL of the Supabase storage bucket
     * @return The bucket URL
     */
    public String getBucketUrl() {
        return String.format("%s/storage/v1/object/public/%s",
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getBucketName());
    }
}
