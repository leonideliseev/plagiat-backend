package ru.itmo.plagiat.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.ai-template-creator")
data class TemplateCreatorProperties(
    val baseUrl: String,
    val createPath: String,
) {
    fun createUrl(): String = baseUrl.trimEnd('/') + "/" + createPath.trimStart('/')
}
