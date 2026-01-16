package kinkmatch.api.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JsonResponseWriter {

    public static void write(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            ErrorResponse body
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
