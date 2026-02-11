package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import ru.itmo.plagiat.dto.server.FindWorksResponse
import ru.itmo.plagiat.service.abstracts.FindService
import ru.itmo.plagiat.service.helper.StorageSelector
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import java.util.Locale

private const val S3_KEY_SEPARATOR = "/"

@Service
class FindServiceImpl(
    private val s3Client: S3Client,
    private val storageSelector: StorageSelector,
) : FindService {
    override fun findWorks(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): FindWorksResponse {
        val storageTarget = storageSelector.select(bucketKey = bucketKey, prefixKey = prefixKey)

        val objectKeyPrefix =
            buildObjectKeyPrefix(
                prefix = storageTarget.prefix,
                workName = workName,
            )

        val allKeys =
            listAllKeys(
                bucket = storageTarget.bucket,
                objectKeyPrefix = objectKeyPrefix,
            )

        val normalizedQueries = normalizeQueries(fileNameQueries)

        val filteredKeys =
            if (normalizedQueries.isEmpty()) {
                allKeys
            } else {
                filterKeysByQueries(
                    keys = allKeys,
                    normalizedQueries = normalizedQueries,
                )
            }

        return FindWorksResponse(
            bucket = storageTarget.bucket,
            keys = filteredKeys,
        )
    }

    private fun buildObjectKeyPrefix(
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

    private fun listAllKeys(
        bucket: String,
        objectKeyPrefix: String,
    ): List<String> {
        val keys = mutableListOf<String>()
        var continuationToken: String? = null

        do {
            val listRequest =
                ListObjectsV2Request
                    .builder()
                    .bucket(bucket)
                    .prefix(objectKeyPrefix)
                    .continuationToken(continuationToken)
                    .build()

            val listResponse = s3Client.listObjectsV2(listRequest)

            val pageKeys =
                listResponse
                    .contents()
                    .orEmpty()
                    .mapNotNull { it.key() }

            keys.addAll(pageKeys)

            continuationToken = listResponse.nextContinuationToken()
        } while (!continuationToken.isNullOrBlank())

        return keys
    }

    private fun normalizeQueries(fileNameQueries: List<String>?): List<String> =
        fileNameQueries
            .orEmpty()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.lowercase(Locale.ROOT) }
            .distinct()
            .toList()

    private fun filterKeysByQueries(
        keys: List<String>,
        normalizedQueries: List<String>,
    ): List<String> =
        keys.filter { key ->
            val keyLowercase = key.lowercase(Locale.ROOT)
            val fileNameLowercase = key.substringAfterLast(S3_KEY_SEPARATOR).lowercase(Locale.ROOT)

            normalizedQueries.any { query ->
                fileNameLowercase.contains(query) || keyLowercase.contains(query)
            }
        }
}
