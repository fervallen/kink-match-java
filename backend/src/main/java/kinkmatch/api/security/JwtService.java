package kinkmatch.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final Key signingKey;
    private final long accessExpirationMinutes;

    public JwtService(
            @Value("${APP_JWT_SECRET}") String secret,
            @Value("${APP_JWT_ACCESS_EXP_MINUTES:15}") long accessExpirationMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMinutes = accessExpirationMinutes;
    }

    public String generateAccessToken(String subjectEmail) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(subjectEmail)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        try {
            Jws<Claims> parsed = parseToken(token);

            String subject = parsed.getBody().getSubject();
            Date expiration = parsed.getBody().getExpiration();

            if (subject == null || subject.isBlank()) return false;
            if (!subject.equals(userDetails.getUsername())) return false;
            if (expiration == null || expiration.before(new Date())) return false;

            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
}
