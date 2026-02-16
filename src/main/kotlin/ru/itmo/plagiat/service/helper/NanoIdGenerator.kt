package ru.itmo.plagiat.service.helper

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.UUID

private const val NANO_ID_LENGTH = 21

@Component
class NanoIdGenerator {
    private val random = SecureRandom()

    fun randomId(): String =
        NanoIdUtils.randomNanoId(
            random,
            NanoIdUtils.DEFAULT_ALPHABET,
            NANO_ID_LENGTH,
        )

    fun randomUuid(): UUID = UUID.randomUUID()

    fun randomUuidString(): String = UUID.randomUUID().toString()
}
