package com.example.KDBS.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    EMAIL_EXISTED(1001, "Email has existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1002, "Username must at least 5 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003, "Password must at least 8 characters", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EXISTED(1004, "Email has not existed", HttpStatus.NOT_FOUND),
    LOGIN_FAILED(1005, "Login failed. Please check your email or password", HttpStatus.FORBIDDEN),
    PHONE_INVALID(1006, "Number phone format invalid.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1007, "Access Denied", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1008, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCHER(1009, "Please enter correct old password", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1010, "Do not match. Please enter correct password", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1011, "OTP invalid. Try again", HttpStatus.BAD_REQUEST),
    USER_IS_BANNED(1012, "User is banned.", HttpStatus.BAD_REQUEST),
    TOO_MANY_OTP_REQUESTS(1013, "Too many OTP requests. Please try again later", HttpStatus.TOO_MANY_REQUESTS),
    OTP_EXPIRED(1014, "OTP has expired. Please request a new one", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_FAILED(1015, "Password reset failed. Please try again", HttpStatus.BAD_REQUEST),
    BUSINESS_LICENSE_EXISTED(1016, "Business license has existed", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(1017, "Post not found with id: %s", HttpStatus.NOT_FOUND),
    CANNOT_SAVE_OWN_POST(1018, "Cannot save your own post", HttpStatus.BAD_REQUEST),
    POST_ALREADY_SAVED(1019, "Post already saved", HttpStatus.BAD_REQUEST),
    POST_NOT_SAVED(1020, "Post not saved", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1021, "User not found", HttpStatus.NOT_FOUND),
    ALREADY_REPORTED(1022, "Already reported this target", HttpStatus.BAD_REQUEST),
    REPORT_NOT_FOUND(1023, "Report not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(1024, "Comment not found with id: %s", HttpStatus.NOT_FOUND),
    EMAIL_PENDING_VERIFICATION(1025, "Email pending verification", HttpStatus.BAD_REQUEST),
    USER_IS_UNVERIFIED(1026, "User is unverified", HttpStatus.BAD_REQUEST),
    STAFF_CANNOT_DELETE_ADMIN_POSTS(1027, "Staff cannot delete admin post", HttpStatus.BAD_REQUEST),

    USER_OR_COMPANY_CAN_ONLY_DELETE_THEIR_OWN_POSTS(1028, "User or Company can only delete their own posts",
            HttpStatus.BAD_REQUEST),
    YOU_DO_NOT_HAVE_PERMISSION_TO_DELETE_THIS_POST(1029, "You do not have permission to delete this post.",
            HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND_AFTER_SAVING(1030, "Post not found after saving", HttpStatus.BAD_REQUEST),
    POST_OWNER_CAN_ONLY_UPDATE_THEIR_OWN_POSTS(1031, "Post owner can only update their own posts",
            HttpStatus.BAD_REQUEST),
    PARENT_COMMENT_NOT_FOUND(1032, "Parent comment not found with id: %s", HttpStatus.BAD_REQUEST),
    STAFF_CANNOT_DELETE_ADMIN_COMMENTS(1033, "Staff cannot delete admin comments", HttpStatus.BAD_REQUEST),
    USER_OR_COMPANY_CAN_ONLY_DELETE_THEIR_OWN_COMMENTS(1034, "User or Company can only delete their own comments",
            HttpStatus.BAD_REQUEST),
    YOU_DO_NOT_HAVE_PERMISSION_TO_DELETE_THIS_COMMENT(1035, "You do not have permission to delete this comment",
            HttpStatus.BAD_REQUEST),
    COMMENT_OWNER_CAN_ONLY_UPDATE_THEIR_OWN_COMMENTS(1036, "Comment owner can only update their own comments",
            HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1038, "Booking not found", HttpStatus.NOT_FOUND),
    INVALID_GUEST_COUNT(1039, "Guest count does not match the provided guest information", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_PAID(1040, "Booking has already been paid", HttpStatus.BAD_REQUEST),
    BOOKING_PAYMENT_FAILED(1041, "Booking payment failed", HttpStatus.BAD_REQUEST),
    TOUR_NOT_FOUND(1042, "Tour not found with id: %s", HttpStatus.NOT_FOUND),
    COMPANY_NOT_FOUND_WITH_EMAIL(1043, "Company not found with email: %s", HttpStatus.NOT_FOUND),
    MAIN_TOUR_IMAGE_IS_REQUIRED(1044, "Main tour image (tourImg) is requried", HttpStatus.NOT_FOUND),
    FAILED_TO_RETRIVE_SAVED_TOUR(1045, "Failed to retrieve saved tour", HttpStatus.NOT_FOUND),
    FAILED_TO_RETRIVE_UPDATED_TOUR(1046, "Failed to retrieve updated tour", HttpStatus.NOT_FOUND),
    IMAGE_NOT_FOUND(1047, "Image not found", HttpStatus.NOT_FOUND),
    TOUR_RATED_IS_EXISTED(1048,"Tour rated is existed",HttpStatus.NOT_FOUND),
    BOOKING_NOT_BELONG_TO_TOUR(1049,"Booking does not belong to this tour",HttpStatus.BAD_REQUEST),
    BOOKING_GUEST_NOT_FOUND(1050,"Booking guest not found",HttpStatus.NOT_FOUND),
    BOOKING_GUEST_NOT_BELONG_TO_BOOKING(1051,"Booking guest does not belong to this booking",HttpStatus.BAD_REQUEST),
    INVALID_PREMIUM_DURATION(1052, "Invalid premium duration. Only 1 or 3 months allowed", HttpStatus.BAD_REQUEST),
    PREMIUM_UPGRADE_FAILED(1053, "Premium upgrade failed", HttpStatus.BAD_REQUEST),
    PREMIUM_ALREADY_ACTIVE(1054, "Premium account is already active", HttpStatus.BAD_REQUEST),
    PREMIUM_ACCESS_DENIED(1055, "Premium account required for this feature", HttpStatus.FORBIDDEN),
    PAYMENT_CREATION_FAILED(1056, "Failed to create payment", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND(1057, "Transaction not found", HttpStatus.NOT_FOUND),
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
    }
}
