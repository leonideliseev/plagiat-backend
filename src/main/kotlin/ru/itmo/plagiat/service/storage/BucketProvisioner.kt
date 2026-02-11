package ru.itmo.plagiat.service.storage

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.util.concurrent.ConcurrentHashMap

private val BUCKET_LOCKS = ConcurrentHashMap<String, Any>()

@Component
class BucketProvisioner(
    private val s3Client: S3Client,
) {
    fun ensureBucketExists(bucket: String) {
        val lock = BUCKET_LOCKS.computeIfAbsent(bucket) { Any() }

        synchronized(lock) {
            if (bucketExists(bucket)) return
            createBucket(bucket)
        }
    }

    private fun bucketExists(bucket: String): Boolean =
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
            true
        } catch (ex: S3Exception) {
            false
        }

    private fun createBucket(bucket: String) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
        } catch (ex: S3Exception) {
            if (!bucketExists(bucket)) throw ex
        }
    }
}
