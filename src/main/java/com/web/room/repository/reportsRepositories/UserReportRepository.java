package com.web.room.repository.reportsRepositories;

import com.web.room.enums.ReportStatus;
import com.web.room.model.reports.RoomReport;
import com.web.room.model.reports.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    @Query("select  r from UserReport r where (:status is null  or r.status = :status)")
    List<UserReport> findByStatus(@Param("status") ReportStatus status);
}
