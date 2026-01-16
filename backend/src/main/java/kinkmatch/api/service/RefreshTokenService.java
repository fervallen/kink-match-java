package kinkmatch.api.service;

import kinkmatch.api.model.RefreshToken;
import kinkmatch.api.repository.RefreshTokenRepository;
import kinkmatch.api.security.TokenUtils;
import kinkmatch.api.service.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class RefreshTokenService {

    public record IssuedRefreshToken(String rawToken, long maxAgeSeconds) {}

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${APP_JWT_REFRESH_EXP_DAYS:14}") long refreshExpirationDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    public IssuedRefreshToken issueFor(kinkmatch.api.model.User user) {
        String rawToken = TokenUtils.randomTokenHex(32); // 32 bytes -> 64 hex chars
        String tokenHash = TokenUtils.sha256Hex(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS));

        refreshTokenRepository.save(refreshToken);

        long maxAgeSeconds = ChronoUnit.DAYS.getDuration().getSeconds() * refreshExpirationDays;
        return new IssuedRefreshToken(rawToken, maxAgeSeconds);
    }

    public RefreshToken validateRawTokenOrThrow(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Missing refresh token");
        }

        String tokenHash = TokenUtils.sha256Hex(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revoke(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    public IssuedRefreshToken rotate(RefreshToken currentRefreshToken) {
        revoke(currentRefreshToken);
        return issueFor(currentRefreshToken.getUser());
    }
}
