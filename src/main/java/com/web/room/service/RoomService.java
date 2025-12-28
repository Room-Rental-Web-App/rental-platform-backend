package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.repository.RoomRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * Room Create karte waqt use hamesha PENDING state (isApprovedByAdmin = false) mein rakhenge.
     */
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

        // IMPORTANT: Naya room by default unapproved rahega
        room.setApprovedByAdmin(false);

        return roomRepository.save(room);
    }

    /**
     * Deletion logic with Cloudinary cleanup.
     */
    @Transactional
    public void deleteRoom(Long id, String email) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized: You do not own this listing");
        }

        // --- Cloudinary Cleanup ---
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

        room.getImageUrls().clear();
        roomRepository.saveAndFlush(room);
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

        // Note: Agar owner room edit karta hai, toh aap chaho toh status wapas false kar sakte ho
        // taaki Admin fir se verify kare. (Optional)
        // existingRoom.setApprovedByAdmin(false);

        return roomRepository.save(existingRoom);
    }

    // Owner apne saare rooms dekh sakta hai (Approved + Pending)
    public List<Room> getRoomsByOwner(String email) {
        return roomRepository.findByOwnerEmail(email);
    }

    // Tenants ko sirf approved rooms dikhenge
    public List<Room> getRoomsByPincode(String pincode) {
        return roomRepository.findByIsApprovedByAdminTrue().stream()
                .filter(r -> r.getPincode().equals(pincode))
                .toList();
    }


    public @Nullable List<Room> findRoom() {
        return roomRepository.findByIsApprovedByAdminTrue();
    }


    public Page<Room> filterRooms(String city, String pincode, String roomType,
                                  Double minPrice, Double maxPrice,
                                  int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return roomRepository.filterRooms(city, pincode, roomType, minPrice, maxPrice, pageable);
    }
}