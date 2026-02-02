package com.web.room.model;

import com.web.room.dto.Request.ShareRoomRequest;
import com.web.room.enums.LookingFor;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class ShareRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private Long ownerId;
    private Long userId;
    private Boolean approvableStatus;

    @Enumerated(EnumType.STRING)
    private LookingFor lookingFor;
    @Min (18)
    @Max (100)
    private Integer ageFrom;
    @Min(18)
    @Max(100)
    private Integer ageTo;

    private  Double price;
    private String description;


}
