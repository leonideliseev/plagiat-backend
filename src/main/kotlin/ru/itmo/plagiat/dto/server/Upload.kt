package ru.itmo.plagiat.dto.server

data class UploadResponse(
    val bucket: String,
    val key: String,
    val sizeBytes: Long,
)
