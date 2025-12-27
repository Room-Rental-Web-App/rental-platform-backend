package com.web.room.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.web.room.model.Room;
import com.web.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {

    @Autowired
    private RoomService roomService;

    private final ObjectMapper objectMapper = new ObjectMapper ();

    /**
     * Endpoint to register a new room listing with media.
     * Expects a JSON string for room data and multipart files for media.
     */
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addRoom(
            @RequestPart("roomData") String roomDataJson,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        System.out.println ("DEBUG: Received request for Add Room");
        try {
            ObjectMapper objectMapper = new ObjectMapper ();
            // Important: Handle Java 8 Date/Time types
//            objectMapper.registerModule(new JavaTimeModule());
            // Important: Don't crash if extra fields are sent from frontend
            objectMapper.configure (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Room room = objectMapper.readValue (roomDataJson, Room.class);

            Room savedRoom = roomService.createRoom (room, images, video);
            return ResponseEntity.ok (savedRoom);

        } catch (Exception e) {
            System.err.println ("CRITICAL ERROR IN CONTROLLER:");
            e.printStackTrace (); // This prints the actual error in IntelliJ
            return ResponseEntity.status (500).body ("Server side error: " + e.getMessage ());
        }
    }

    /**
     * Endpoint to update room details.
     * Validates owner email to prevent unauthorized modifications.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long id,
            @RequestBody Room roomDetails,
            @RequestParam String email) {
        try {
            Room updatedRoom = roomService.updateRoom (id, roomDetails, email);
            return ResponseEntity.ok (updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status (403).body (e.getMessage ());
        }
    }

    /**
     * Endpoint to delete a room listing.
     * Triggers cleanup of associated files in Cloudinary.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRoom(
            @PathVariable Long id,
            @RequestParam String email) {
        try {
            roomService.deleteRoom (id, email);
            return ResponseEntity.ok (Map.of ("message", "Listing and associated media deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status (403).body (e.getMessage ());
        }
    }

    /**
     * Searches for rooms based on location pincode.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Room>> searchByPincode(@RequestParam String pincode) {
        // This line was causing the error if the method was missing in Service
        return ResponseEntity.ok (roomService.getRoomsByPincode (pincode));
    }

    /**
     * Retrieves all listings created by a specific owner.
     */
    @GetMapping("/my-listings")
    public ResponseEntity<List<Room>> getMyListings(@RequestParam String email) {
        return ResponseEntity.ok (roomService.getRoomsByOwner (email));
    }

    @GetMapping("/findRoom")
    public ResponseEntity<List<Room>> findRoom() {
        return ResponseEntity.ok (roomService.findRoom ());
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Room>> filterRooms(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        System.out.println (" city: " + city + " pincode" + pincode + " roomType: " + roomType + " minPrice: " + minPrice + " maxPrice: " + maxPrice + " page: " + page + " size: " + size  );
        return ResponseEntity.ok(
                roomService.filterRooms(city, pincode, roomType, minPrice, maxPrice, page, size)
        );
    }


}