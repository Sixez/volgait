package ru.sixez.volgait.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.service.JwtService;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {
    private static final String SECRET_KEY = "566a684d5547383257465a4661316c4d556c4e76555842684c33704b54557730576d597855454e35556b4d3253484d3252455930533274744c3356684d6b35796130784c545573355a6b46355a45314c6432313655773d3d";
    private static final long LIFETIME = Duration.ofMinutes(15).toMillis();
    private static final List<String> BLACKLIST = new ArrayList<>();

    private Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parser();
        parser.verifyWith(getSecretKey());

        return parser.build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSecretKey() {
        byte[] bytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    // JwtService impl

    @Override
    public String generateToken(UserDetails user) {
        return generateToken(new HashMap<>(), user);
    }

    @Override
    public String generateToken(Map<String, Object> claims, UserDetails user) {
        JwtBuilder builder = Jwts.builder();
        builder.subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + LIFETIME))
                .claims(claims)
                .signWith(getSecretKey(), Jwts.SIG.HS256);
        return builder.compact();
    }

    @Override
    public void revoke(String token) {
        if (!isRevoked(token)) {
            BLACKLIST.add(token);
        }
    }

    @Override
    public boolean isRevoked(String token) {
        return BLACKLIST.contains(token);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    @Override
    public boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public boolean validate(String token, UserDetails details) {
        if (token == null || details == null)
            return false;
        String username = extractUsername(token);

        return username.equals(details.getUsername()) && !isExpired(token);
    }
}
