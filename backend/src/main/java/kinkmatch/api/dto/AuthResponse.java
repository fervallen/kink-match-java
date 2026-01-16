package kinkmatch.api.dto;

public class AuthResponse {

    public String accessToken;

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
