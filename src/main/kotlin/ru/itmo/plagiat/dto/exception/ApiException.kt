package ru.itmo.plagiat.dto.exception

enum class ApiExceptionCode {
    INVALID_UPLOAD,
    INVALID_STORAGE,
}

data class ErrorResponse(
    val code: String,
    val message: String,
)

open class ApiException(
    val code: String,
    override val message: String,
) : RuntimeException(message) {
    constructor(code: ApiExceptionCode, message: String) : this(
        code = code.name.lowercase(),
        message = message,
    )
}

class InvalidUploadException(
    message: String,
) : ApiException(code = ApiExceptionCode.INVALID_UPLOAD, message = message)

class InvalidStorageException(
    message: String,
) : ApiException(code = ApiExceptionCode.INVALID_STORAGE, message = message)
