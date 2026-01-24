package com.web.room.repository.reportsRepositories;

import com.web.room.model.reports.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {}
