package com.web.room.model.reports;
import com.web.room.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "user_reports")
@Getter @Setter
public class UserReport extends Report {

    // User who is being reported by room_owner
    @ManyToOne(optional = false)
    private User reportedUser;
}
