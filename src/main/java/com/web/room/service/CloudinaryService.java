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
     * Uploads a multipart file to a specific Cloudinary folder.
     * Uses "resource_type: auto" to support both images and videos.
     * @param file The file to upload.
     * @param folder The target folder in Cloudinary.
     * @return The secure URL of the uploaded asset.
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary Upload Error: " + e.getMessage());
        }
    }

    /**
     * Deletes a file from Cloudinary using its Public ID.
     * @param publicId The unique identifier of the file in Cloudinary.
     * @param resourceType The type of resource ("image" or "video").
     */
    public void deleteFile(String publicId, String resourceType) {
        try {
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId,
                        ObjectUtils.asMap("resource_type", resourceType));
                System.out.println("Resource deleted from Cloudinary: " + publicId);
            }
        } catch (Exception e) {
            System.err.println("Cloudinary Deletion Failed for ID: " + publicId + " - " + e.getMessage());
        }
    }
}