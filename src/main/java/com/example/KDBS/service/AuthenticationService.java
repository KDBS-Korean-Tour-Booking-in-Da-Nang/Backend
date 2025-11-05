package com.example.KDBS.service;

import com.example.KDBS.dto.request.AuthenticationRequest;
import com.example.KDBS.dto.request.LogOutRequest;
import com.example.KDBS.dto.request.UsernameAuthenticationRequest;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.enums.Status;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.UserMapper;
import com.example.KDBS.model.InvalidateToken;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.InvalidateTokenRepository;
import com.example.KDBS.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final InvalidateTokenRepository invalidtokenrepository;
    private final UserMapper userMapper;
    protected static final String signature = "OG3aRIYXHjOowyfI2MOHbl8xSjoF/B/XwkK6b276SfXAhL3KbizWWuT8LB1YUVvh";

    private final PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        User user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.WRONG_EMAIL));

        if (Status.UNVERIFIED.equals(user.getStatus())) {
            throw new AppException(ErrorCode.WRONG_EMAIL);
        }
        if (Status.BANNED.equals(user.getStatus())) {
            throw new AppException(ErrorCode.USER_IS_BANNED);
        }

        return authenticateAndBuildResponse(user, authenticationRequest.getPassword());
    }

    public AuthenticationResponse loginWithUsername(UsernameAuthenticationRequest usernameAuthenticationRequest) {
        User user = userRepository.findByUsername(usernameAuthenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_EXISTED));

        return authenticateAndBuildResponse(user, usernameAuthenticationRequest.getPassword());
    }

    // Shared authentication logic
    private AuthenticationResponse authenticateAndBuildResponse(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        String token = generateToken(user);
        UserResponse userResponse = userMapper.toUserResponse(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .user(userResponse)
                .build();
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        String subject = (user.getEmail() != null && !user.getEmail().isEmpty())
                ? user.getEmail()
                : user.getUsername();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("KDBS.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.HOURS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", user.getUserId())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signature.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildScope(User user) {
        if (user.getRole() != null && !user.getRole().toString().isEmpty()) {
            return "" + user.getRole();
        }
        return "";

    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signature.getBytes(StandardCharsets.UTF_8));
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Verify signature
        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Check expiration using the token's actual exp claim
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime == null || !expiryTime.after(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Check if token is blacklisted
        if (invalidtokenrepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public void logout(LogOutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken());

            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidateToken invalidatedToken = InvalidateToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();

            invalidtokenrepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already invalid or expired: {}", exception.getMessage());
        }
    }
}
