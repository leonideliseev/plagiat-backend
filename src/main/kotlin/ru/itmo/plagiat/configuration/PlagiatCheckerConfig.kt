package ru.itmo.plagiat.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.plagiat-checker")
data class PlagiatCheckerProperties(
    val baseUrl: String,
    val checkPath: String,
    val jobsPath: String,
) {
    fun checkUrl(): String = baseUrl.trimEnd('/') + "/" + checkPath.trimStart('/')

    fun jobUrl(jobId: String): String = baseUrl.trimEnd('/') + "/" + jobsPath.trimStart('/') + "/" + jobId
}
