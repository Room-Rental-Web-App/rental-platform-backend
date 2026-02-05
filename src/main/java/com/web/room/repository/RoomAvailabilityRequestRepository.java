package com.web.room.repository;

import com.web.room.model.Room;
import com.web.room.model.RoomAvailabilityRequest;
import com.web.room.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomAvailabilityRequestRepository extends JpaRepository<RoomAvailabilityRequest, Long>
{
    boolean existsByUserAndRoom(User user, Room room);
    Optional<RoomAvailabilityRequest> findByUserAndRoom(User user, Room room);
    List<RoomAvailabilityRequest> findByRoomIdAndNotifiedFalse(Long roomId);
}
