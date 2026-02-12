package ru.itmo.plagiat.dto.client

data class FilesAnalyzeRequest(
    val id: String,
    val files: List<AnalyzeFile>,
)

data class AnalyzeFile(
    val name: String,
    val code: String,
)
