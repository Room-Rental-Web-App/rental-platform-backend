package com.web.room.controller;

import com.web.room.service.RoomAvailabilityRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room_availability")
@RequiredArgsConstructor
public class RoomAvailabilityRequestController {
    private final RoomAvailabilityRequestService service;

    @PostMapping("/add")
    public ResponseEntity<?> addNotify(@RequestParam Long userId,@RequestParam Long roomId){
         return service.addRequest(userId, roomId);
    }

    @GetMapping("/find_by_userId/{userId}")
    public ResponseEntity<?> findByUserId(@PathVariable Long userId){
        return service.findByUserId(userId);
    }

    @PutMapping("/updateNotify")
    public ResponseEntity<?> updateNotify(@RequestParam Long notifyId,@RequestParam boolean status ){
        System.out.println (notifyId + " " + status);
        return service.updateNotify(notifyId, status);
    }
}
