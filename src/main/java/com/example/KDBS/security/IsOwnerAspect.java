package com.example.KDBS.security;

import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class IsOwnerAspect {

    @Before("@annotation(isOwner)")
    public void checkOwnership(JoinPoint jp, IsOwner isOwner) {
        String targetParam = isOwner.param();
        String jwtClaim = isOwner.jwtClaim();

        // 1) Lấy email từ tham số method (param, path, body, part)
        String requestedEmail = extractParamByName(jp, targetParam);
        if (requestedEmail == null) {
            throw new AppException(ErrorCode.MISSING_PARAMETER);
        }

        // 2) Lấy email từ JWT đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String authEmail = extractEmailFromAuth(auth, jwtClaim);
        if (authEmail == null) {
            throw new AppException(ErrorCode.CANNOT_RESOLVE_EMAIL_FROM_TOKEN);
        }

        // 3) So khớp (không phân biệt hoa/thường)
        if (!authEmail.equalsIgnoreCase(requestedEmail)) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_ANOTHER_ACCOUNT);
        }
    }

    private String extractParamByName(JoinPoint jp, String targetName) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        Method method = sig.getMethod();
        Object[] args = jp.getArgs();
        Annotation[][] paramAnns = method.getParameterAnnotations();
        String[] paramNames = sig.getParameterNames();

        // 1) Ưu tiên @RequestParam / @PathVariable (như code cũ)
        for (int i = 0; i < args.length; i++) {
            for (Annotation ann : paramAnns[i]) {
                if (ann instanceof RequestParam rp && targetName.equals(rp.value())) {
                    return args[i] != null ? args[i].toString() : null;
                }
                if (ann instanceof PathVariable pv && targetName.equals(pv.value())) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }
        }

        // 2) Fallback: khớp theo tên biến param (ví dụ: String email, String userEmail)
        if (paramNames != null) {
            for (int i = 0; i < args.length; i++) {
                if (targetName.equals(paramNames[i])) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }
        }

        // 3) NEW: tìm trong các DTO @RequestBody / @RequestPart hoặc object param
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;

            boolean isBodyLike = false;
            for (Annotation ann : paramAnns[i]) {
                if (ann instanceof RequestBody || ann instanceof RequestPart) {
                    isBodyLike = true;
                    break;
                }
            }

            // Nếu có @RequestBody/@RequestPart thì chắc chắn đây là DTO
            // Nếu không có, nhưng type là custom object (không phải String, primitive wrapper) vẫn có thể thử
            if (isBodyLike || isProbablyDto(arg)) {
                Object value = getFieldValueByPath(arg, targetName);
                if (value != null) {
                    return value.toString();
                }
            }
        }

        return null;
    }

    /** Đoán xem đây có phải DTO không (rất đơn giản) */
    private boolean isProbablyDto(Object arg) {
        Package p = arg.getClass().getPackage();
        if (p == null) return false;
        String pkgName = p.getName();
        // tuỳ dự án, bạn có thể chỉnh cho đúng package DTO của bạn
        return pkgName.contains(".dto") || pkgName.contains(".request") || pkgName.contains(".model");
    }

    /**
     * Hỗ trợ cả dạng "email" hoặc "user.email" (nếu sau này bạn muốn nested path)
     */
    private Object getFieldValueByPath(Object obj, String path) {
        if (obj == null || path == null) return null;

        String[] parts = path.split("\\.");
        Object current = obj;

        for (String part : parts) {
            if (current == null) return null;
            current = getSingleFieldValue(current, part);
        }
        return current;
    }

    private Object getSingleFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    private String extractEmailFromAuth(Authentication auth, String jwtClaim) {
        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String v = jwt.getClaimAsString(jwtClaim);
            if (v == null && "sub".equals(jwtClaim)) {
                // fallback sang "email" nếu bạn set email ở claim "email"
                v = jwt.getClaimAsString("email");
            }
            return v;
        }
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        if (principal instanceof String s) {
            return s;
        }
        return null;
    }
}
