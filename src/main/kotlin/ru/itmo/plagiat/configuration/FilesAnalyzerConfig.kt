package ru.itmo.plagiat.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.files-analyzer")
data class FilesAnalyzerProperties(
    val baseUrl: String,
    val analyzePath: String,
    val jobsPath: String,
) {
    fun analyzeUrl(): String = baseUrl.trimEnd('/') + "/" + analyzePath.trimStart('/')

    fun jobUrl(jobId: String): String = baseUrl.trimEnd('/') + "/" + jobsPath.trimStart('/') + "/" + jobId
}
