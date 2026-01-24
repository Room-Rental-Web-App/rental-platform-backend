package com.web.room.model.reports;

import com.web.room.model.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "room_reports")
@Getter
@Setter
public class RoomReport extends Report {

    // Room who is being reported by another user
    @ManyToOne(optional = false)
    private Room room;
}
