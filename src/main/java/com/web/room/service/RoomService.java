package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.repository.RoomRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public Room createRoom(Room room, List<MultipartFile> images, MultipartFile video) {
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadFile(file, "room_listings/images");
                    imageUrls.add(url);
                }
            }
        }
        room.setImageUrls(imageUrls);

        if (video != null && !video.isEmpty()) {
            String videoUrl = cloudinaryService.uploadFile(video, "room_listings/videos");
            room.setVideoUrl(videoUrl);
        }
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long id, String email) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized: You do not own this listing");
        }

        // --- STEP 1: Cloudinary Cleanup ---
        if (room.getImageUrls() != null) {
            for (String url : room.getImageUrls()) {
                String pid = extractPublicId(url);
                if (pid != null) cloudinaryService.deleteFile(pid, "image");
            }
        }
        if (room.getVideoUrl() != null) {
            String vid = extractPublicId(room.getVideoUrl());
            if (vid != null) cloudinaryService.deleteFile(vid, "video");
        }

        // --- STEP 2: Database Constraint Fix ---
        // Pehle child table (room_images) se data clear karein
        room.getImageUrls().clear();
        roomRepository.saveAndFlush(room); // Force database update

        // --- STEP 3: Final Delete ---
        roomRepository.delete(room);
    }

    private String extractPublicId(String url) {
        if (url == null || !url.contains("/upload/")) return null;
        try {
            String part = url.split("/upload/")[1];
            String idWithExt = part.substring(part.indexOf("/") + 1);
            return idWithExt.substring(0, idWithExt.lastIndexOf("."));
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public Room updateRoom(Long roomId, Room updatedDetails, String currentUserEmail) {
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!existingRoom.getOwnerEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new RuntimeException("Unauthorized edit attempt.");
        }

        existingRoom.setTitle(updatedDetails.getTitle());
        existingRoom.setDescription(updatedDetails.getDescription());
        existingRoom.setPrice(updatedDetails.getPrice());
        existingRoom.setAddress(updatedDetails.getAddress());
        existingRoom.setPincode(updatedDetails.getPincode());
        existingRoom.setCity(updatedDetails.getCity());
        existingRoom.setContactNumber(updatedDetails.getContactNumber());
        existingRoom.setRoomType(updatedDetails.getRoomType());

        return roomRepository.save(existingRoom);
    }

    public List<Room> getRoomsByOwner(String email) {
        return roomRepository.findByOwnerEmail(email);
    }

    public List<Room> getRoomsByPincode(String pincode) {
        return roomRepository.findByPincode(pincode);
    }

    public @Nullable List<Room> findRoom() {
        return roomRepository.findAll ();
    }
}