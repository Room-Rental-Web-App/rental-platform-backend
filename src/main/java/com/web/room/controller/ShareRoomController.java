package com.web.room.controller;

import com.web.room.dto.Request.ShareRoomRequest;
import com.web.room.service.ShareRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/share_room")
@RequiredArgsConstructor
public class ShareRoomController {
    private final ShareRoomService service;

    @PostMapping("/add")
    public ResponseEntity<?> addShareRoom(@RequestBody ShareRoomRequest req){
        return service.addShareRoom(req);
    }

    @PutMapping("/approvalStatus/{approvalStatus}/{roomShareId}")
    public ResponseEntity<?> changeApprovalStatus(@PathVariable boolean approvalStatus, @PathVariable Long roomShareId){
        return service.changeApprovalStatus(approvalStatus, roomShareId);
    }
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getByOwnerId(@PathVariable Long ownerId){
        return service.getByOwnerId(ownerId);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable Long userId){
        return service.getByUserId(userId);
    }
    @DeleteMapping("/{shareRoomId}")
    public ResponseEntity<?> deleteById(@PathVariable Long shareRoomId){
        return service.deleteById(shareRoomId);
    }

}
