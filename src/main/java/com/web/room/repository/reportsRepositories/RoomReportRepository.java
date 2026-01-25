package com.web.room.repository.reportsRepositories;

import com.web.room.enums.ReportStatus;
import com.web.room.model.reports.RoomReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomReportRepository extends JpaRepository<RoomReport, Long> {

    @Query("SELECT r FROM RoomReport r WHERE (:status is null or r.status =:status)")
    List<RoomReport> findByStatus(@Param ("status") ReportStatus status);
}
