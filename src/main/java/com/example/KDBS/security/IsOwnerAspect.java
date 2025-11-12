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
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class IsOwnerAspect {

    @Before("@annotation(isOwner)")
    public void checkOwnership(JoinPoint jp, IsOwner isOwner) {
        String targetParam = isOwner.param();
        String jwtClaim = isOwner.jwtClaim();

        // Lấy email từ tham số method (ưu tiên theo tên @RequestParam/@PathVariable)
        String requestedEmail = extractParamByName(jp, targetParam);
        if (requestedEmail == null) {
            throw new AppException(ErrorCode.MISSING_PARAMETER);
        }

        //  Lấy email từ JWT đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
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

        for (int i = 0; i < args.length; i++) {
            // Ưu tiên đọc tên từ @RequestParam / @PathVariable
            for (Annotation ann : paramAnns[i]) {
                if (ann instanceof RequestParam rp && targetName.equals(rp.value())) {
                    return args[i] != null ? args[i].toString() : null;
                }
                if (ann instanceof PathVariable pv && targetName.equals(pv.value())) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }
        }
        // Fallback: khớp theo tên biến (cần biên dịch với -parameters, nhưng nhiều IDE đã bật)
        for (int i = 0; i < args.length; i++) {
            if (paramNames != null && targetName.equals(paramNames[i])) {
                return args[i] != null ? args[i].toString() : null;
            }
        }
        return null;
    }

    private String extractEmailFromAuth(Authentication auth, String jwtClaim) {
        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String v = jwt.getClaimAsString(jwtClaim);
            if (v == null && "sub".equals(jwtClaim)) {
                // thử fallback sang "email" nếu bạn set email ở claim "email"
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
