package kinkmatch.api.api;

import jakarta.servlet.http.HttpServletRequest;
import kinkmatch.api.service.exceptions.ConflictException;
import kinkmatch.api.service.exceptions.NotFoundException;
import kinkmatch.api.service.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage(),
                        (first, second) -> first
                ));

        ErrorResponse response = ErrorResponse.of(
                400,
                "Bad Request",
                "Validation failed",
                request.getRequestURI()
        );
        response.fieldErrors = fieldErrors;
        return response;
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException exception, HttpServletRequest request) {
        return ErrorResponse.of(409, "Conflict", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return ErrorResponse.of(404, "Not Found", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(Exception exception, HttpServletRequest request) {
        String message = (exception instanceof BadCredentialsException)
                ? "Invalid email or password"
                : exception.getMessage();

        return ErrorResponse.of(401, "Unauthorized", message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAny(Exception exception, HttpServletRequest request) {
        return ErrorResponse.of(500, "Internal Server Error", "Unexpected error", request.getRequestURI());
    }
}
