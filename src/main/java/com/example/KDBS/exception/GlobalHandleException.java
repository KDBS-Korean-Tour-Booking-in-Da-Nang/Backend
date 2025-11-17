package com.example.KDBS.exception;

import com.example.KDBS.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandleException {

    private static final Logger logger = LoggerFactory.getLogger(GlobalHandleException.class);

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        logger.error("AppException: Code: {}, Message: {}", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1001);  // Mã lỗi tùy chỉnh
        apiResponse.setMessage(exception.getMessage());

        logger.error("CustomException: Message: {}", exception.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgument(MethodArgumentNotValidException ex) {

        String message = ex.getFieldError() != null
                ? ex.getFieldError().getDefaultMessage()
                : "Validation failed";

        ApiResponse<Void> api = new ApiResponse<>();
        api.setCode(ErrorCode.MISSING_PARAMETER.getCode());   // hoặc tạo ErrorCode.VALIDATION_FAILED
        api.setMessage(message);

        return ResponseEntity
                .status(ErrorCode.MISSING_PARAMETER.getStatusCode())
                .body(api);
    }
    @ExceptionHandler({ AuthorizationDeniedException.class, AccessDeniedException.class })
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationDenied(Exception ex) {
        ApiResponse<Void> api = new ApiResponse<>();
        api.setCode(ErrorCode.FORBIDDEN.getCode());
        api.setMessage(ErrorCode.FORBIDDEN.getMessage());

        logger.error("Authorization error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(api);
    }


    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(Exception exception){
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.RUN_TIME_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.RUN_TIME_EXCEPTION.getMessage());

        logger.error("Exception: Code: {}, Message: {}", ErrorCode.RUN_TIME_EXCEPTION.getCode(), ErrorCode.RUN_TIME_EXCEPTION.getMessage(), exception);
        return ResponseEntity.status(ErrorCode.RUN_TIME_EXCEPTION.getStatusCode()).body(apiResponse);
    }
}
