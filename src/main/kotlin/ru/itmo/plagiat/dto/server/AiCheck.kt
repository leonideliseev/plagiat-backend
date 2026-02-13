package ru.itmo.plagiat.dto.server

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateAiCheckJobRequest(
    val fileNameQueries: List<String>?,
)

data class CreateAiCheckJobResponse(
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

data class GetAiCheckJobResponse(
    val id: String,
    val status: String,
    val verdict: Map<String, VerdictEntry>?,
)

data class VerdictEntry(
    @JsonProperty("final_ai_probability")
    val finalAiProbability: Double,
    val confidence: String,
    @JsonProperty("chunks_analyzed")
    val chunksAnalyzed: Int,
    @JsonProperty("main_reasons")
    val mainReasons: List<String>,
)
