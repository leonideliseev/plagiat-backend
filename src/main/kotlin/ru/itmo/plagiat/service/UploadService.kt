package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.configuration.S3Properties
import ru.itmo.plagiat.dto.exception.InvalidUploadException
import ru.itmo.plagiat.dto.server.UploadResponse
import ru.itmo.plagiat.service.helper.ObjectKeyFactory
import ru.itmo.plagiat.service.helper.UploadValidator
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class UploadService(
    private val client: S3Client,
    private val properties: S3Properties,
    private val keyFactory: ObjectKeyFactory,
    private val validator: UploadValidator,
) {
    fun uploadTask(
        taskName: String,
        files: List<MultipartFile>,
    ): List<UploadResponse> {
        if (files.isEmpty()) throw InvalidUploadException("Список файлов пустой")

        return files.map { file ->
            validator.validateZip(file)

            val originalName = file.originalFilename ?: throw InvalidUploadException("Нет имени файла")
            val surnameName = extractSurnameName(originalName)

            val bytes = file.bytes
            val key =
                keyFactory.build(
                    taskName = taskName,
                    surnameName = surnameName,
                )

            val req =
                PutObjectRequest
                    .builder()
                    .apply {
                        bucket(properties.bucket)
                        this.key(key)
                        contentType("application/zip")
                    }.build()

            client.putObject(req, RequestBody.fromBytes(bytes))

            UploadResponse(
                bucket = properties.bucket,
                key = key,
                sizeBytes = bytes.size.toLong(),
            )
        }
    }

    private fun extractSurnameName(originalFileName: String): String {
        val base = originalFileName.substringAfterLast("/").substringAfterLast("\\")
        if (!base.lowercase().endsWith(".zip")) throw InvalidUploadException("Нужен zip архив")

        val stem = base.removeSuffix(".zip").removeSuffix(".ZIP")

        val parts =
            stem
                .trim()
                .split(Regex("[ _]+"))
                .filter { it.isNotBlank() }

        if (parts.size < 2) {
            throw InvalidUploadException("Имя файла должно содержать фамилию и имя")
        }

        val surname = parts[0]
        val name = parts[1]

        return "${surname}_$name"
    }
}
