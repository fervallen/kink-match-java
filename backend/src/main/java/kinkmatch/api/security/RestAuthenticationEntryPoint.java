package kinkmatch.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kinkmatch.api.api.ErrorResponse;
import kinkmatch.api.api.JsonResponseWriter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {

        System.out.println(
                "[SECURITY 401] method=" + request.getMethod()
                        + " path=" + request.getRequestURI()
                        + " message=" + authenticationException.getMessage()
        );

        ErrorResponse body = ErrorResponse.of(
                401,
                "Unauthorized",
                "Authentication required or token invalid",
                request.getRequestURI()
        );

        JsonResponseWriter.write(response, objectMapper, 401, body);
    }
}
