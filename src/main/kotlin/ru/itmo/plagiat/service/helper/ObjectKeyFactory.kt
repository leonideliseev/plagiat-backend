package ru.itmo.plagiat.service.helper

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class ObjectKeyFactory(
    private val clock: Clock,
) {
    private val fmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS").withZone(ZoneOffset.UTC)

    fun build(
        taskName: String,
        surnameName: String,
    ): String {
        val safeTask = sanitizePathPart(taskName)
        val safeSurnameName = sanitizeFileStem(surnameName)
        val ts = fmt.format(Instant.now(clock))

        return "$safeTask/${safeSurnameName}_$ts.zip"
    }

    private fun sanitizePathPart(s: String?): String = sanitize(s)

    private fun sanitizeFileStem(s: String?): String = sanitize(s)

    private fun sanitize(s: String?): String {
        if (s.isNullOrBlank()) return "unknown"

        return s
            .trim()
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^A-Za-z0-9_\\-\\p{IsCyrillic}]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .ifBlank { "unknown" }
    }
}
