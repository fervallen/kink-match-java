package kinkmatch.api.bootstrap;

import kinkmatch.api.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupJob(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanup() {
        Instant now = Instant.now();

        long expiredDeleted = refreshTokenRepository.deleteByExpiresAtBefore(now);

        Instant revokedCutoff = now.minus(7, ChronoUnit.DAYS);
        long revokedDeleted = refreshTokenRepository.deleteByRevokedIsTrueAndRevokedAtBefore(revokedCutoff);

        if (expiredDeleted > 0 || revokedDeleted > 0) {
            System.out.println(
                    "[CLEANUP] refresh_tokens expiredDeleted=" + expiredDeleted
                            + " revokedDeleted=" + revokedDeleted
            );
        }
    }
}
