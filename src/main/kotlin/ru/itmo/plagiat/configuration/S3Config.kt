package ru.itmo.plagiat.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class S3Config(
    private val properties: S3Properties,
) {
    @Bean
    fun s3Client(): S3Client =
        S3Client
            .builder()
            .apply {
                endpointOverride(URI.create(properties.endpoint))
                region(Region.of(properties.region))
                credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey, properties.secretKey),
                    ),
                )
                serviceConfiguration(
                    S3Configuration
                        .builder()
                        .apply {
                            pathStyleAccessEnabled(true)
                        }.build(),
                )
            }.build()
}

@ConfigurationProperties(prefix = "app.s3")
data class S3Properties(
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val prefix: String,
)
