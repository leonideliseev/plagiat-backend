package ru.itmo.plagiat.service.storage

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request

@Component
class S3ObjectLister(
    private val s3Client: S3Client,
) {
    fun listAllKeys(
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

            keys.addAll(
                listResponse
                    .contents()
                    .orEmpty()
                    .mapNotNull { it.key() },
            )

            continuationToken = listResponse.nextContinuationToken()
        } while (!continuationToken.isNullOrBlank())

        return keys
    }
}
