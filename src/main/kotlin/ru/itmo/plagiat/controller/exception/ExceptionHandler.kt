package ru.itmo.plagiat.controller.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.plagiat.dto.exception.ErrorResponse
import ru.itmo.plagiat.dto.exception.InvalidUploadException

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(InvalidUploadException::class)
    fun handleInvalidUpload(ex: InvalidUploadException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(code = "invalid_upload", message = ex.message ?: "Некорректный запрос"),
        )
}
