package ru.itmo.plagiat.service.storage

import org.springframework.stereotype.Component
import ru.itmo.plagiat.util.S3_KEY_SEPARATOR
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val UNKNOWN_VALUE = "unknown"
private const val UNDERSCORE = "_"
private const val ZIP_EXTENSION = ".zip"
private const val TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss_SSS"

private val TIMESTAMP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter
        .ofPattern(TIMESTAMP_PATTERN)
        .withZone(ZoneOffset.UTC)

private val WHITESPACE_REGEX = Regex("""\s+""")
private val FORBIDDEN_CHARS_REGEX = Regex("""[^\p{L}\p{N}_-]+""")
private val MULTIPLE_UNDERSCORES_REGEX = Regex("""_+""")

@Component
class S3ObjectKeyFactory(
    private val clock: Clock,
) {
    fun build(
        prefix: String,
        workName: String,
        surnameName: String,
    ): String {
        val sanitizedPrefix = sanitize(prefix)
        val sanitizedWorkName = sanitize(workName)
        val sanitizedSurnameName = sanitize(surnameName)

        val timestamp = TIMESTAMP_FORMATTER.format(Instant.now(clock))

        val fileName = sanitizedSurnameName + UNDERSCORE + timestamp + ZIP_EXTENSION
        val keySegments = listOf(sanitizedPrefix, sanitizedWorkName, fileName)

        return keySegments.joinToString(S3_KEY_SEPARATOR)
    }

    private fun sanitize(rawValue: String?): String {
        if (rawValue.isNullOrBlank()) return UNKNOWN_VALUE

        val sanitizedValue =
            rawValue
                .trim()
                .replace(WHITESPACE_REGEX, UNDERSCORE)
                .replace(FORBIDDEN_CHARS_REGEX, UNDERSCORE)
                .replace(MULTIPLE_UNDERSCORES_REGEX, UNDERSCORE)
                .trim(UNDERSCORE.single())

        return sanitizedValue.ifBlank { UNKNOWN_VALUE }
    }
}
