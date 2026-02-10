package com.web.room.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.room.model.Room;
import com.web.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // Use a single ObjectMapper instance for better performance
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ConfigurableServletWebServerFactory configurableServletWebServerFactory;

    /**
     * Endpoint to register a new room listing with media.
     * Modified to catch specific Limit Validation errors.
     */
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addRoom(
            @RequestPart("roomData") String roomDataJson,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        System.out.println (roomDataJson);
        try {
            // Configure mapper to handle primitives and extra fields
            objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


            Room room = objectMapper.readValue(roomDataJson, Room.class);

            Room savedRoom = roomService.createRoom(room, images, video);
            return ResponseEntity.ok(savedRoom);

        } catch (RuntimeException e) {
            // Catch the 'Limit Reached' error from RoomService
            if (e.getMessage().contains("Limit reached")) {
                return ResponseEntity.status(403).body(Map.of(
                        "status", "LIMIT_EXCEEDED",
                        "message", e.getMessage()
                ));
            }
            return ResponseEntity.status(400).body(e.getMessage());

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR IN CONTROLLER: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint to update room details.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long id,
            @RequestBody Room roomDetails,
            @RequestParam String email) {
        try {
            Room updatedRoom = roomService.updateRoom(id, roomDetails, email);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PutMapping("/update-status/{id}/{newStatus}")
    public ResponseEntity<?> updateAvailabilityStatus(@PathVariable Long id, @PathVariable boolean newStatus){
        System.out.println (id + " " + newStatus    );
         return roomService.updateAvailabilityStatus(id, newStatus);
    }


    /**
     * Endpoint to delete a room listing.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRoom(
            @PathVariable Long id,
            @RequestParam String email) {
        try {
            roomService.deleteRoom(id, email);
            return ResponseEntity.ok(Map.of("message", "Listing deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Room>> searchByPincode(@RequestParam String pincode) {
        return ResponseEntity.ok(roomService.getRoomsByPincode(pincode));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<List<Room>> getMyListings(@RequestParam String email) {
        return ResponseEntity.ok(roomService.getRoomsByOwner(email));
    }

    @GetMapping("/findRoom")
    public ResponseEntity<List<Room>> findRoom() {
        return ResponseEntity.ok(roomService.findRoom());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Room>> getFeaturedRooms() {
        return ResponseEntity.ok(roomService.getFeaturedRooms(6));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Room>> filterRooms(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            @RequestParam(required = false, defaultValue = "2") Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(
                roomService.filterRooms(city, pincode, roomType, minPrice, maxPrice,
                        userLat, userLng, radiusKm, page, size)
        );
    }

    @GetMapping("/roomDetails/{roomId}")
    public ResponseEntity<Room> getRoomDetails(@PathVariable Long roomId){
        return ResponseEntity.ok(roomService.getRoomDetails(roomId));
    }

    @GetMapping("/roomCount/{ownerEmail}")
    public long getRoomCount(@PathVariable String ownerEmail) {
        return roomService.getRoomCount(ownerEmail);
    }

    @GetMapping("/cities")
    public List<String> getCitiesCovered(){
        return roomService.getCitiesCovered();
    }

    @PatchMapping("/{roomId}/increment-contact")
    public ResponseEntity<?> incrementContact(@PathVariable Long roomId) {
        roomService.incrementContactCount(roomId);
        return ResponseEntity.ok().body(Map.of("message", "Contact count updated"));
    }
}