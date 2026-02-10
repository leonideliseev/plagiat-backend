package ru.itmo.plagiat.dto.exception

data class ErrorResponse(
    val code: String,
    val message: String,
)

class InvalidUploadException(
    message: String,
) : RuntimeException(message)
