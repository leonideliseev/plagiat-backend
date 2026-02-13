package ru.itmo.plagiat.dto.server

import java.time.OffsetDateTime

data class CreatePlagiatCheckJobRequest(
    val fileNameQueries: List<String>?,
)

data class CreatePlagiatCheckJobResponse(
    val jobId: String,
)

data class GetPlagiatCheckJobResponse(
    val id: String,
    val leftPersonNames: List<String>,
    val rightPersonNames: List<String>,
    val status: String,
    val avg: List<Double>,
    val max: List<Double>,
    val hasError: Boolean,
    val createdAt: OffsetDateTime,
)
