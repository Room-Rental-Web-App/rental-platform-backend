package com.web.room.dto.Request;

import com.web.room.enums.ReportType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReportRequest {

    private ReportType reportType;

    private Long targetId;   // roomId OR userId OR ownerId
    private String reason;
}
