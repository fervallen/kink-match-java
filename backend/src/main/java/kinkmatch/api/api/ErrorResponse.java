package kinkmatch.api.api;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    public Instant timestamp = Instant.now();
    public int status;
    public String error;
    public String message;
    public String path;
    public Map<String, String> fieldErrors;

    public static ErrorResponse of(int status, String error, String message, String path) {
        ErrorResponse response = new ErrorResponse();
        response.status = status;
        response.error = error;
        response.message = message;
        response.path = path;
        return response;
    }
}
