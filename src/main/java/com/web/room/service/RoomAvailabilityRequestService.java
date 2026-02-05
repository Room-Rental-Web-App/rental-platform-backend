package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.RoomAvailabilityRequest;
import com.web.room.model.User;
import com.web.room.repository.RoomAvailabilityRequestRepository;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityRequestService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomAvailabilityRequestRepository requestRepository;
    public ResponseEntity<?>    addRequest(Long userId, Long roomId) {

        Room room = roomRepository.findById (roomId).orElseThrow (()-> new EntityNotFoundException ("Room not found"));
        User user = userRepository.findById (userId).orElseThrow (()-> new EntityNotFoundException ("User not found"));

        // 🔥 DUPLICATE CHECK
        Optional<RoomAvailabilityRequest> existingRequest = requestRepository.findByUserAndRoom(user, room);

        if (existingRequest.isPresent()) {

            RoomAvailabilityRequest req = existingRequest.get();

            if (!req.getNotified()) {
                return ResponseEntity.badRequest().body("You are already waiting for notification for this room");
            }

            // allow re-subscribe
            req.setNotified(false);
            requestRepository.save(req);

            return ResponseEntity.ok("You will be notified when the room is available again");
        }

        RoomAvailabilityRequest request = new RoomAvailabilityRequest ();
        request.setRoom (room);
        request.setUser (user);
        requestRepository.save (request);
        return ResponseEntity.ok ("We will notify you when the room becomes available");
    }
}
