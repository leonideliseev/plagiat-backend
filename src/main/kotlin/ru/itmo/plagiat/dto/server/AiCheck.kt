package ru.itmo.plagiat.dto.server

data class CheckAiRequest(
    val fileNameQueries: List<String>?,
)

data class CheckAiResponse(
    val jobId: String,
    val bucket: String,
    val archiveKey: String?,
    val selectedFiles: List<SelectedArchiveFile>,
)

data class SelectedArchiveFile(
    val pathInArchive: String,
    val sizeBytes: Long,
    val linesCount: Int,
)
