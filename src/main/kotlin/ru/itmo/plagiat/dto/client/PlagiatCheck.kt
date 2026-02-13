package ru.itmo.plagiat.dto.client

data class PlagiatCheckerRequest(
    val jobId: String,
    val keys: List<String>,
    val bucket: String,
)
