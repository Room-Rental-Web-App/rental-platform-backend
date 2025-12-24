package com.web.room.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.room.model.Room;
import com.web.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {

    @Autowired
    private RoomService roomService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Updated Endpoint: Consolidates text data into one JSON part
     */
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addRoom(
            @RequestPart("roomData") String roomDataJson, // Saara text yahan aayega
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        try {
            Room room = objectMapper.readValue(roomDataJson, Room.class);

            Room savedRoom = roomService.createRoom(room, images, video);
            return ResponseEntity.ok(savedRoom);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding room: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails, @RequestParam String email) {
        try {
            Room updatedRoom = roomService.updateRoom(id, roomDetails, email);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id, @RequestParam String email) {
        try {
            roomService.deleteRoom(id, email);
            return ResponseEntity.ok(Map.of("message", "Room deleted successfully"));
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
}