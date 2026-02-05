package com.web.room.controller;

import com.web.room.service.RoomAvailabilityRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/room_availability")
@RequiredArgsConstructor
public class RoomAvailabilityRequestController {
    private final RoomAvailabilityRequestService service;

    @PostMapping("/add")
    public ResponseEntity<?> addNotify(@RequestParam Long userId,@RequestParam Long roomId){
         return service.addRequest(userId, roomId);
    }
}
