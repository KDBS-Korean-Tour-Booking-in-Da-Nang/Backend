package com.example.KDBS.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsOwner {
    //Tên tham số method cần so khớp quyền sở hữu
    String param() default "email";

    //Tên claim trong JWT lấy email
    String jwtClaim() default "sub";
}
