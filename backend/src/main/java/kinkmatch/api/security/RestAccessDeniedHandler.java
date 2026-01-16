package kinkmatch.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kinkmatch.api.api.ErrorResponse;
import kinkmatch.api.api.JsonResponseWriter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        System.out.println(
                "[SECURITY 403] method=" + request.getMethod()
                        + " path=" + request.getRequestURI()
                        + " message=" + accessDeniedException.getMessage()
        );

        ErrorResponse body = ErrorResponse.of(
                403,
                "Forbidden",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );

        JsonResponseWriter.write(response, objectMapper, 403, body);
    }
}
