package ru.itmo.plagiat.service.storage

import org.springframework.stereotype.Component
import ru.itmo.plagiat.util.S3_KEY_SEPARATOR

@Component
class S3KeyPrefixBuilder {
    fun build(
        prefix: String,
        workName: String,
    ): String {
        val normalizedPrefix = prefix.trim().trim(S3_KEY_SEPARATOR.single())
        val normalizedWorkName = workName.trim().trim(S3_KEY_SEPARATOR.single())

        return listOf(normalizedPrefix, normalizedWorkName)
            .filter { it.isNotBlank() }
            .joinToString(S3_KEY_SEPARATOR)
            .plus(S3_KEY_SEPARATOR)
    }
}
