package kinkmatch.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kinkmatch.api.dto.AuthLoginRequest;
import kinkmatch.api.dto.AuthRegisterRequest;
import kinkmatch.api.dto.AuthResponse;
import kinkmatch.api.security.JwtService;
import kinkmatch.api.security.RefreshCookieService;
import kinkmatch.api.service.RefreshTokenService;
import kinkmatch.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieService refreshCookieService;

    public AuthController(
            UserService userService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService,
            RefreshCookieService refreshCookieService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.refreshCookieService = refreshCookieService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request, HttpServletResponse response) {
        var user = userService.createUser(request.email, request.password);

        var issuedRefreshToken = refreshTokenService.issueFor(user);
        refreshCookieService.setRefreshTokenCookie(response, issuedRefreshToken.rawToken(), issuedRefreshToken.maxAgeSeconds());

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        return new AuthResponse(accessToken);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email, request.password)
        );

        var user = userService.findEntityByEmail(request.email);

        var issuedRefreshToken = refreshTokenService.issueFor(user);
        refreshCookieService.setRefreshTokenCookie(response, issuedRefreshToken.rawToken(), issuedRefreshToken.maxAgeSeconds());

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        return new AuthResponse(accessToken);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response
    ) {
        var currentRefreshToken = refreshTokenService.validateRawTokenOrThrow(refreshToken);
        var rotatedRefreshToken = refreshTokenService.rotate(currentRefreshToken);

        refreshCookieService.setRefreshTokenCookie(response, rotatedRefreshToken.rawToken(), rotatedRefreshToken.maxAgeSeconds());

        String email = currentRefreshToken.getUser().getEmail();
        String accessToken = jwtService.generateAccessToken(email);

        return new AuthResponse(accessToken);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response
    ) {
        if (!refreshToken.isBlank()) {
            try {
                var currentRefreshToken = refreshTokenService.validateRawTokenOrThrow(refreshToken);
                refreshTokenService.revoke(currentRefreshToken);
            } catch (Exception ignored) {
                // Logout is idempotent; ignore invalid/expired token
            }
        }

        refreshCookieService.clearRefreshTokenCookie(response);
    }
}
