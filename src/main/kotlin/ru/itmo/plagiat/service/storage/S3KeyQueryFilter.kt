package ru.itmo.plagiat.service.storage

import org.springframework.stereotype.Component
import ru.itmo.plagiat.util.S3_KEY_SEPARATOR
import java.util.Locale

@Component
class S3KeyQueryFilter {
    fun filter(
        keys: List<String>,
        fileNameQueries: List<String>?,
    ): List<String> {
        val normalizedQueries = normalize(fileNameQueries)
        if (normalizedQueries.isEmpty()) return keys

        return keys.filter { key ->
            val keyLowercase = key.lowercase(Locale.ROOT)
            val fileNameLowercase = key.substringAfterLast(S3_KEY_SEPARATOR).lowercase(Locale.ROOT)

            normalizedQueries.any { query ->
                fileNameLowercase.contains(query) || keyLowercase.contains(query)
            }
        }
    }

    private fun normalize(fileNameQueries: List<String>?): List<String> =
        fileNameQueries
            .orEmpty()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.lowercase(Locale.ROOT) }
            .distinct()
            .toList()
}
