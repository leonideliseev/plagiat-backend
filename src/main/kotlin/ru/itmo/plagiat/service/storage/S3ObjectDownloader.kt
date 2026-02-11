package ru.itmo.plagiat.service.storage

import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

@Component
class S3ObjectDownloader(
    private val s3Client: S3Client,
) {
    fun downloadBytes(
        bucket: String,
        key: String,
    ): ByteArray {
        val request =
            GetObjectRequest
                .builder()
                .bucket(bucket)
                .key(key)
                .build()
        return s3Client.getObject(request, ResponseTransformer.toBytes()).asByteArray()
    }
}
