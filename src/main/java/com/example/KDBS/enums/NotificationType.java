package com.example.KDBS.enums;

public enum NotificationType {
    // Forum notifications
    LIKE_POST,
    LIKE_COMMENT,
    COMMENT_POST,
    REPLY_COMMENT,
    
    // Tour & Booking notifications
    NEW_BOOKING,             // Company nhận khi có booking mới
    BOOKING_CONFIRMED,       // User nhận khi booking được xác nhận
    BOOKING_UPDATE_REQUEST,  // User nhận khi company yêu cầu update booking
    TOUR_APPROVED,           // Company nhận khi tour được approve
    NEW_RATING,              // Company nhận khi có rating mới
    BOOKING_REJECTED,        // User nhận khi booking bị reject
    BOOKING_UPDATED_BY_USER, // Company nhận khi user cập nhật booking theo yêu cầu
    BOOKING_PENDING_DEPOSIT, // User nhận khi cần thanh toán tiền cọc
    BOOKING_PENDING_BALANCE, // User nhận khi cần thanh toán số tiền còn lại
    BOOKING_WAITING_APPROVAL, // Company nhận khi booking đang chờ được approve
    BOOKING_FAILED,          // User/Company nhận khi booking bị failed
    BOOKING_SUCCESS_WAIT_CONFIRM, // User/Company nhận khi tour đã kết thúc, đợi confirm
    BOOKING_UNDER_COMPLAINT,  // Company nhận khi booking bị complaint
    BOOKING_SUCCESS,          // User/Company nhận khi booking hoàn thành
    BOOKING_CANCELLED         // Company nhận khi user cancel booking

}
