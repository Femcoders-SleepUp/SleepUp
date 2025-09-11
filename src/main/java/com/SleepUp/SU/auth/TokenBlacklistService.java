package com.SleepUp.SU.auth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    private final AuthServiceHelper authServiceHelper;

    public void addToBlacklist(String token) {
        try {
            Claims claims = authServiceHelper.validateAccessToken(token);

            Date expiration = claims.getExpiration();
            blacklistedTokens.put(token, expiration.getTime());
        } catch (Exception e){
            long expirationTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000);
            blacklistedTokens.put(token, expirationTime);
        }
    }

    public boolean isTokenInBlacklist(String token) {
        Long expirationTime = blacklistedTokens.get(token);

        if (expirationTime == null){
            return false;
        }

        if (System.currentTimeMillis() > expirationTime) {
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }

    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }

    @Scheduled(fixedRate = 3600000)
    public void removeExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }

    public int getBlacklistedTokensCount() {
        return blacklistedTokens.size();
    }
}
