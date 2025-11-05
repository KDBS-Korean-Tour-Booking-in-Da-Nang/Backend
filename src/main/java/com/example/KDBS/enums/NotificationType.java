package com.example.KDBS.enums;

public enum NotificationType {
    // Forum notifications
    LIKE_POST,
    LIKE_COMMENT,
    COMMENT_POST,
    REPLY_COMMENT,
    
    // Tour & Booking notifications
    NEW_BOOKING,           // Company nhận khi có booking mới
    BOOKING_CONFIRMED,     // User nhận khi booking được xác nhận
    BOOKING_UPDATE_REQUEST,// User nhận khi company yeu cau update booking
    TOUR_APPROVED,         // Company nhận khi tour được approve
    NEW_RATING,            // Company nhận khi có rating mới
    BOOKING_REJECTED       // User nhan khi booking bi reject

}
