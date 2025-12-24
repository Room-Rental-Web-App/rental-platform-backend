package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Business logic to create a new room listing
     * @param room Room object containing text data
     * @param images List of image files
     * @param video Video file (optional)
     * @return Saved Room object
     */
    public Room createRoom(Room room, List<MultipartFile> images, MultipartFile video) {

        // 1. Handle Multiple Image Uploads
        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = cloudinaryService.uploadFile(file, "room_listings/images");
                uploadedImageUrls.add(url);
            }
        }
        room.setImageUrls(uploadedImageUrls);

        // 2. Handle Video Upload (If exists)
        if (video != null && !video.isEmpty()) {
            String videoUrl = cloudinaryService.uploadFile(video, "room_listings/videos");
            room.setVideoUrl(videoUrl);
        }

        // 3. Save to Database
        return roomRepository.save(room);
    }

    // 1. Update Room Logic
    public Room updateRoom(Long roomId, Room updatedDetails, String currentUserEmail) {
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Check ownership before updating
        if (!existingRoom.getOwnerEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to edit this room!");
        }

        // Update fields
        existingRoom.setTitle(updatedDetails.getTitle());
        existingRoom.setDescription(updatedDetails.getDescription());
        existingRoom.setPrice(updatedDetails.getPrice());
        existingRoom.setAddress(updatedDetails.getAddress());
        existingRoom.setPincode(updatedDetails.getPincode());
        existingRoom.setContactNumber(updatedDetails.getContactNumber());
        existingRoom.setRoomType(updatedDetails.getRoomType());

        return roomRepository.save(existingRoom);
    }

    // 2. Delete Room Logic
    public void deleteRoom(Long roomId, String currentUserEmail) {
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Check ownership before deleting
        if (!existingRoom.getOwnerEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to delete this room!");
        }

        roomRepository.delete(existingRoom);
    }
    public List<Room> getRoomsByPincode(String pincode) {
        return roomRepository.findByPincode(pincode);
    }

    public List<Room> getAllApprovedRooms() {
        return roomRepository.findByIsApprovedByAdminTrue();
    }
    public List<Room> getRoomsByOwner(String email) {
        return roomRepository.findByOwnerEmail(email);

    }
}
