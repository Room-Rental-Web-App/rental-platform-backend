package com.web.room.model.reports;
import com.web.room.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "room_owner_reports")
@Getter @Setter
public class RoomOwnerReport extends Report {

    // Room owner who is being reported by another user
    @ManyToOne(optional = false)
    private User reportedOwner;

}
