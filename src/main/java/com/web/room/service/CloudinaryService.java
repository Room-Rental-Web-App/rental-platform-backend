package com.web.room.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Uploads a multipart file to Cloudinary.
     * Optimized to handle large files (videos) using InputStreams and Chunking.
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // InputStream use karne par Cloudinary ko batana padta hai ki hum kya bhej rahe hain
            Map options = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto",
                    "chunk_size", 6000000
            );

            // FIX: file.getBytes() ya file.getInputStream() ko bytes mein convert karke bhejna sabse safe hai
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Cloudinary Upload Error: " + e.getMessage());
        }
    }    /**
     * Deletes a file from Cloudinary using its Public ID.
     */
    public void deleteFile(String publicId, String resourceType) {
        try {
            if (publicId != null && !publicId.isEmpty()) {
                // Ensure resourceType is explicitly "image" or "video"
                Map response = cloudinary.uploader().destroy(publicId,
                        ObjectUtils.asMap("resource_type", resourceType));

                System.out.println("Cloudinary Deletion Result for " + publicId + ": " + response.get("result"));
            }
        } catch (Exception e) {
            System.err.println("Cloudinary Deletion Failed: " + e.getMessage());
        }
    }
}