package com.SleepUp.SU.security.jwt;

import com.SleepUp.SU.config.properties.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Getter
public class JwtService {

    private static final String ROLE_CLAIM = "roles";
    private static final long DEFAULT_REFRESH_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000;

    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final long jwtRefreshExpirationMs;

    public JwtService(AppProperties appProperties) {
        AppProperties.JwtProperties jwt = appProperties.getJwt();
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwt.getSecret()));
        this.jwtExpirationMs = jwt.getExpirationMs();
        this.jwtRefreshExpirationMs = jwt.getRefreshExpirationMs() != null ? jwt.getRefreshExpirationMs() : DEFAULT_REFRESH_EXPIRATION_MS;
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE_CLAIM, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("iat", System.currentTimeMillis());

        return buildToken(claims, userDetails.getUsername(), jwtRefreshExpirationMs);
    }


    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE_CLAIM, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("iat", System.currentTimeMillis());

        return buildToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }


    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(Instant.now()));
    }

    public boolean isValidToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken, UserDetails userDetails) {
        if (isValidToken(refreshToken) && extractUsername(refreshToken).equals(userDetails.getUsername())) {
            return generateAccessToken(userDetails);
        }
        throw new RuntimeException("Invalid or expired refresh token");
    }

}
