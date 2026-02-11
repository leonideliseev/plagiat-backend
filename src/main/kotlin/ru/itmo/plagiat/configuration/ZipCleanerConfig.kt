package ru.itmo.plagiat.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.zip-cleaner")
data class ZipCleanerProperties(
    val delete: Delete = Delete(),
    val keep: Keep = Keep(),
) {
    data class Delete(
        val dirs: List<String> = emptyList(),
        val files: List<String> = emptyList(),
        val extensions: List<String> = emptyList(),
    )

    data class Keep(
        val onlyAllowed: Boolean = true,
        val allowedExtensions: List<String> = emptyList(),
        val specialNames: List<String> = emptyList(),
    )
}
