package com.web.room.enums;

public enum SupportIssueType {

    // Payments & Billing
    PAYMENT,
    REFUND,
    INVOICE,

    // Room / Property
    ROOM_LISTING,
    ROOM_UNAVAILABLE,
    ROOM_CONDITION,
    FAKE_LISTING,
    PRICE_MISMATCH,

    // Owner / User Issues
    OWNER_PROBLEM,
    OWNER_NOT_RESPONDING,
    USER_BEHAVIOR,

    // Booking & Stay
    BOOKING_ISSUE,
    CHECK_IN_PROBLEM,
    CHECK_OUT_PROBLEM,
    CANCELLATION,

    // Account & App
    ACCOUNT,
    LOGIN_ISSUE,
    VERIFICATION_ISSUE,
    PROFILE_UPDATE,

    // Technical
    APP_BUG,
    PAYMENT_FAILURE,
    NOTIFICATION_ISSUE,

    // Safety & Legal
    SAFETY_CONCERN,
    FRAUD_REPORT,
    POLICY_VIOLATION,

    // Catch-all
    OTHER
}
