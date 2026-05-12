package br.com.fiap.ecocompliance.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> fieldErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<FieldErrorDetail> fieldErrors) {
        this(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }

    public record FieldErrorDetail(String campo, String mensagem) {}
}
