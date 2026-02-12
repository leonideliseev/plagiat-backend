package ru.itmo.plagiat.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.s3")
data class S3Properties(
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val buckets: Map<String, String>,
    val prefixes: Map<String, String>,
    val defaults: Defaults,
    val autoBucketsCreating: AutoBucketsCreating = AutoBucketsCreating(),
) {
    data class Defaults(
        val bucketKey: String,
        val prefixKey: String,
    )

    data class AutoBucketsCreating(
        val enabled: Boolean = true,
    )
}
