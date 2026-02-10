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

    private val spaces = Regex("""\s+""")
    private val forbidden = Regex("""[^\p{L}\p{N}_-]+""")
    private val manyUnderscores = Regex("""_+""")

    fun build(
        taskName: String,
        surnameName: String,
    ): String {
        val safeTask = sanitize(taskName)
        val safeSurnameName = sanitize(surnameName)
        val ts = fmt.format(Instant.now(clock))
        return "$safeTask/${safeSurnameName}_$ts.zip"
    }

    private fun sanitize(value: String?): String {
        if (value.isNullOrBlank()) return "unknown"

        val cleaned =
            value
                .trim()
                .replace(spaces, "_")
                .replace(forbidden, "_")
                .replace(manyUnderscores, "_")
                .trim('_')

        return cleaned.ifBlank { "unknown" }
    }
}
