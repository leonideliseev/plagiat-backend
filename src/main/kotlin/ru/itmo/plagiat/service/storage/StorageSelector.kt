package ru.itmo.plagiat.service.storage

import org.springframework.stereotype.Component
import ru.itmo.plagiat.configuration.S3Properties
import ru.itmo.plagiat.dto.exception.InvalidStorageException
import ru.itmo.plagiat.util.ERROR_MESSAGE_BUCKET_KEY_NOT_ALLOWED
import ru.itmo.plagiat.util.ERROR_MESSAGE_PREFIX_KEY_NOT_ALLOWED

@Component
class StorageSelector(
    private val s3Properties: S3Properties,
) {
    fun select(
        bucketKey: String?,
        prefixKey: String?,
    ): StorageTarget {
        val resolvedBucketKey = bucketKey?.takeIf { it.isNotBlank() } ?: s3Properties.defaults.bucketKey
        val resolvedPrefixKey = prefixKey?.takeIf { it.isNotBlank() } ?: s3Properties.defaults.prefixKey

        val bucket =
            s3Properties.buckets[resolvedBucketKey]
                ?: throw InvalidStorageException(ERROR_MESSAGE_BUCKET_KEY_NOT_ALLOWED)

        val prefix =
            s3Properties.prefixes[resolvedPrefixKey]
                ?: throw InvalidStorageException(ERROR_MESSAGE_PREFIX_KEY_NOT_ALLOWED)

        return StorageTarget(bucket = bucket, prefix = prefix)
    }
}

data class StorageTarget(
    val bucket: String,
    val prefix: String,
)
