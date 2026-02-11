package ru.itmo.plagiat.dto.server

data class FindWorksRequest(
    val fileNameQueries: List<String>?,
)

data class FindWorksResponse(
    val bucket: String,
    val keys: List<String>,
)
