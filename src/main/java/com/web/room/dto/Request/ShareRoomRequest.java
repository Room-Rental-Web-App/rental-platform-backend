package com.web.room.dto.Request;

import com.web.room.enums.LookingFor;

import lombok.Data;

@Data
public class ShareRoomRequest {
    private Long roomId;
    private Long ownerId;
    private Long userId;
    private Boolean approvableStatus;

    private LookingFor lookingFor;
    private Integer ageFrom;
    private Integer ageTo;

    private Double price;
    private String description;
}
