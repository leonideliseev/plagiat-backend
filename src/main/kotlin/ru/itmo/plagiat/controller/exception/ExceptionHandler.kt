package ru.itmo.plagiat.controller.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.plagiat.dto.exception.ApiException
import ru.itmo.plagiat.dto.exception.ErrorResponse

const val ERROR_MESSAGE_BAD_REQUEST_DEFAULT = "Некорректный запрос"

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                code = ex.code,
                message = ex.message.ifBlank { ERROR_MESSAGE_BAD_REQUEST_DEFAULT },
            ),
        )
}
