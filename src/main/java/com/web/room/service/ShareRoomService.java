package com.web.room.service;

import com.cloudinary.api.exceptions.NotFound;
import com.web.room.dto.Request.ShareRoomRequest;
import com.web.room.enums.LookingFor;
import com.web.room.model.Room;
import com.web.room.model.ShareRoom;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.ShareRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class ShareRoomService {
    private final ShareRoomRepository shareRoomRepository;
    private final RoomRepository roomRepository;

    public ResponseEntity<?> addShareRoom(ShareRoomRequest req) {
        ShareRoom shareRoom = new ShareRoom ();

        shareRoom.setRoomId (req.getRoomId ());
        shareRoom.setOwnerId (req.getOwnerId ());
        shareRoom.setUserId (req.getUserId ());
        shareRoom.setApprovableStatus (Boolean.FALSE);

        shareRoom.setLookingFor (req.getLookingFor ());
        shareRoom.setAgeFrom (req.getAgeFrom ());
        shareRoom.setAgeTo (req.getAgeTo ());
        shareRoom.setDescription (req.getDescription ());
        shareRoom.setPrice (req.getPrice ());

        return ResponseEntity.ok (shareRoomRepository.save (shareRoom));
    }

    public ResponseEntity<?> changeApprovalStatus(boolean approvalStatus, Long roomShareId) {
        ShareRoom room = shareRoomRepository.findById (roomShareId).orElseThrow (()->new EntityNotFoundException("Room not found with id: " + roomShareId));
        room.setApprovableStatus (approvalStatus);
        return ResponseEntity.ok ("Approval Status Updated Successfully");

    }

    public ResponseEntity<?> getByOwnerId(Long ownerId) {
        return ResponseEntity.ok(shareRoomRepository.findByOwnerId(ownerId));
    }

    public ResponseEntity<?> getByUserId(Long userId) {
        return ResponseEntity.ok(shareRoomRepository.findByUserId(userId));
    }

    public ResponseEntity<?> deleteById(Long shareRoomId) {
        shareRoomRepository.deleteById (shareRoomId);
        return ResponseEntity.ok("Shared Room Removed");
    }
}
