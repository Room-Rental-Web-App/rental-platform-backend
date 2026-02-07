package com.web.room.dto.Response;

import com.web.room.model.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class RoomAvailabilityResponse {
    Long id;
    private Long userId;
    private Room room;
    private Boolean notified;
    private LocalDateTime created;
}
