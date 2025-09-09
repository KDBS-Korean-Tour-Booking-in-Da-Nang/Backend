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
    UNAUTHENTICATED(1008,"Unauthenticated", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCHER(1009,"Please enter correct old password", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1010,"Do not match. Please enter correct password", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1011,"OTP invalid. Try again" ,HttpStatus.BAD_REQUEST ),
    USER_IS_BANNED(1012,"User is banned.",HttpStatus.BAD_REQUEST),
    TOO_MANY_OTP_REQUESTS(1013, "Too many OTP requests. Please try again later", HttpStatus.TOO_MANY_REQUESTS),
    OTP_EXPIRED(1014, "OTP has expired. Please request a new one", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_FAILED(1015, "Password reset failed. Please try again", HttpStatus.BAD_REQUEST),
    BUSINESS_LICENSE_EXISTED(1016, "Business license has existed", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(1017, "Post not found", HttpStatus.NOT_FOUND),
    CANNOT_SAVE_OWN_POST(1018, "Cannot save your own post", HttpStatus.BAD_REQUEST),
    POST_ALREADY_SAVED(1019, "Post already saved", HttpStatus.BAD_REQUEST),
    POST_NOT_SAVED(1020, "Post not saved", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1021, "User not found", HttpStatus.NOT_FOUND),
    ALREADY_REPORTED(1022, "Already reported this target", HttpStatus.BAD_REQUEST),
    REPORT_NOT_FOUND(1023, "Report not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(1024, "Comment not found", HttpStatus.NOT_FOUND),
    EMAIL_PENDING_VERIFICATION(1025,"Email pending verification",HttpStatus.BAD_REQUEST),
    USER_IS_UNVERIFIED(1026,"User is unverified",HttpStatus.BAD_REQUEST);

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
