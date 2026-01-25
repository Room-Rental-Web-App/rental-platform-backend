package com.web.room.repository.reportsRepositories;

import com.web.room.enums.ReportStatus;
import com.web.room.model.reports.RoomOwnerReport;
import com.web.room.model.reports.RoomReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface RoomOwnerReportRepository extends JpaRepository<RoomOwnerReport, Long> {
    @Query("select  r from RoomOwnerReport r where (:status is null  or r.status = :status)")
    List<RoomOwnerReport> findByStatus(@Param("status") ReportStatus status);
}
