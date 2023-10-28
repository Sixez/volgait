package ru.sixez.volgait.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String generateToken(UserDetails user);
    String generateToken(Map<String, Object> claims, UserDetails user);

    void revoke(String token);
    boolean isRevoked(String token);

    String extractUsername(String token);
    Date extractExpiration(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    boolean isExpired(String token);
    boolean validate(String token, UserDetails details);
}
