package com.example.KDBS.utils;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtils {
    public static Integer getCurrentUserId() {
        try {
            String header = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest().getHeader("Authorization");

            String token = header.substring(7);
            SignedJWT jwt = SignedJWT.parse(token);

            return ((Number) jwt.getJWTClaimsSet().getClaim("userId")).intValue();
        } catch (Exception e) {
            return null;
        }
    }
}