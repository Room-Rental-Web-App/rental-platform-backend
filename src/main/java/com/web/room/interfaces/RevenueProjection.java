package com.web.room.interfaces;

import java.time.LocalDate;

public interface RevenueProjection {
    LocalDate getDate();
    Long getCount();
    Long getTotalAmount();
}
