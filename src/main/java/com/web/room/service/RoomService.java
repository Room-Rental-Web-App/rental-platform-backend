package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.Subscription;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final CloudinaryService cloudinaryService;
    private final SubscriptionRepository subscriptionRepo;
    private final EmailService emailService;

    /**
     * Creates a room after strictly validating the owner's subscription limit.
     */
    public Room createRoom(Room room, List<MultipartFile> images, MultipartFile video) {

        // 1. SECURITY CHECK: Validate Room Limit before any file processing
        validateRoomLimit(room.getOwnerEmail());

        // 2. Process File Uploads (Only happens if limit check passes)
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

        // 3. Set Default Persistence Values
        room.setApprovedByAdmin(false);
        room.setAvailable(true);
        room.setContactViewCount(0);
        room.setCreatedAt(LocalDateTime.now());

        return roomRepository.save(room);
    }

    /**
     * Enforces the NEW strictly reduced room limits.
     */
    private void validateRoomLimit(String email) {
        long currentRooms = roomRepository.countByOwnerEmail(email);

        Optional<Subscription> subOpt = subscriptionRepo.findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc(
                email, "ROLE_OWNER", LocalDateTime.now()
        );

        int allowedLimit = 2; // Default Free Tier

        if (subOpt.isPresent()) {
            String plan = subOpt.get().getPlanCode();

            // STRICT REDUCED LIMITS MAPPING
            if (plan.contains("7D")) {
                allowedLimit = 3;      // Trial
            } else if (plan.contains("30D")) {
                allowedLimit = 6;      // Monthly
            } else if (plan.contains("180D")) {
                allowedLimit = 15;     // Half-Yearly
            } else if (plan.contains("365D")) {
                allowedLimit = 40;     // Yearly
            }
        }

        if (currentRooms >= allowedLimit) {
            throw new RuntimeException("Limit reached! You have " + currentRooms +
                    " rooms. Upgrade your plan to increase your limit beyond " + allowedLimit + " rooms.");
        }
    }

    // ... [Rest of the methods: deleteRoom, updateRoom, etc. remain unchanged]

    @Transactional
    public void deleteRoom(Long id, String email) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized: You do not own this listing");
        }

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
        Room existingRoom = roomRepository.findById (roomId).orElseThrow (() -> new RuntimeException ("Room not found"));

        if (!existingRoom.getOwnerEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new RuntimeException("Unauthorized edit attempt.");
        }
        System.out.println ("\n update room \n");
        existingRoom.setTitle(updatedDetails.getTitle());
        existingRoom.setDescription(updatedDetails.getDescription());
        existingRoom.setPrice(updatedDetails.getPrice());
        existingRoom.setAddress(updatedDetails.getAddress());
        existingRoom.setPincode(updatedDetails.getPincode());
        existingRoom.setCity(updatedDetails.getCity());
        existingRoom.setContactNumber(updatedDetails.getContactNumber());
        existingRoom.setRoomType(updatedDetails.getRoomType());

        if (updatedDetails.getAvailable() != null) {
            existingRoom.setAvailable(updatedDetails.getAvailable());
        }

        return roomRepository.save(existingRoom);
    }

    public List<Room> getRoomsByOwner(String email) {
        return roomRepository.findByOwnerEmail(email);
    }

    public List<Room> getRoomsByPincode(String pincode) {
        return roomRepository.findByApprovedByAdminTrue().stream()
                .filter(r -> r.getPincode().equals(pincode))
                .toList();
    }

    public List<Room> findRoom() {
        return roomRepository.findByApprovedByAdminTrue();
    }

    public Page<Room> filterRooms(String city, String pincode, String roomType, Double minPrice, Double maxPrice, Double userLat, Double userLng, Double radiusKm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("priorityScore"), Sort.Order.desc("createdAt")));
        return roomRepository.filterRoomsWithRadius(city, pincode, roomType, minPrice, maxPrice, userLat, userLng, radiusKm, pageable);
    }

    public Room getRoomDetails(Long roomId) {
        return roomRepository.findById(roomId).orElse(null);
    }

    public long getRoomCount(String ownerEmail) {
        return roomRepository.countByOwnerEmail(ownerEmail);
    }

    public List<String> getCitiesCovered() {
        return roomRepository.findAllCities();
    }

    public List<Room> getFeaturedRooms(int limit) {
        return roomRepository.findTop6ByOrderByIdDesc();
    }

    @Transactional
    public void incrementContactCount(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Integer currentCount = room.getContactViewCount();
        if (currentCount == null) currentCount = 0;

        room.setContactViewCount(currentCount + 1);

        if (room.getContactViewCount() >= 5) {
            room.setPriorityScore(100);
        }

        roomRepository.save(room);
    }

    @Transactional
    public ResponseEntity<?> updateAvailabilityStatus(Long id, boolean newStatus) {
        Room room = roomRepository.findById (id).orElseThrow (() -> new RuntimeException ("Room not found"));
        room.setAvailable (newStatus);
        Room updatedRoom = roomRepository.save (room);

        if (newStatus){
            emailService.notifyWaitingUsers(room);
        }

        return ResponseEntity.ok ( updatedRoom);
    }


}