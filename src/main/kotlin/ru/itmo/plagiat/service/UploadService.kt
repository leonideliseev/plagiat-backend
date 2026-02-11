package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.configuration.S3Properties
import ru.itmo.plagiat.dto.exception.InvalidUploadException
import ru.itmo.plagiat.dto.server.UploadResponse
import ru.itmo.plagiat.service.helper.BucketProvisioner
import ru.itmo.plagiat.service.helper.ObjectKeyFactory
import ru.itmo.plagiat.service.helper.StorageSelector
import ru.itmo.plagiat.service.helper.UploadValidator
import ru.itmo.plagiat.service.helper.ZipCleaner
import ru.itmo.plagiat.util.ERROR_MESSAGE_EMPTY_FILE_LIST
import ru.itmo.plagiat.util.ERROR_MESSAGE_FILENAME_SHOULD_CONTAIN_SURNAME_AND_NAME
import ru.itmo.plagiat.util.ERROR_MESSAGE_NO_FILENAME
import ru.itmo.plagiat.util.ZIP_CONTENT_TYPE
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.Locale

private const val UNIX_PATH_SEPARATOR = "/"
private const val WINDOWS_PATH_SEPARATOR = "\\"
private const val EXTENSION_SEPARATOR = "."
private const val UNDERSCORE = "_"

private const val PERSON_NAME_SEPARATOR_PATTERN = """[ _]+"""
private val PERSON_NAME_SEPARATOR_REGEX = Regex(PERSON_NAME_SEPARATOR_PATTERN)

@Service
class UploadService(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties,
    private val objectKeyFactory: ObjectKeyFactory,
    private val uploadValidator: UploadValidator,
    private val zipCleaner: ZipCleaner,
    private val storageSelector: StorageSelector,
    private val bucketProvisioner: BucketProvisioner,
) {
    fun uploadWork(
        workName: String,
        files: List<MultipartFile>,
        bucketKey: String?,
        prefixKey: String?,
    ): List<UploadResponse> {
        if (files.isEmpty()) throw InvalidUploadException(ERROR_MESSAGE_EMPTY_FILE_LIST)

        val storageTarget = storageSelector.select(bucketKey = bucketKey, prefixKey = prefixKey)

        if (s3Properties.autoBucketsCreating.enabled) {
            bucketProvisioner.ensureBucketExists(storageTarget.bucket)
        }

        return files.map { file ->
            uploadValidator.validateZip(file)

            val originalFileName =
                file.originalFilename
                    ?: throw InvalidUploadException(ERROR_MESSAGE_NO_FILENAME)

            val surnameAndName = extractSurnameAndName(originalFileName)

            val cleanedZipBytes = zipCleaner.cleanZip(file.bytes)

            val objectKey =
                objectKeyFactory.build(
                    prefix = storageTarget.prefix,
                    workName = workName,
                    surnameName = surnameAndName,
                )

            val putObjectRequest =
                PutObjectRequest
                    .builder()
                    .bucket(storageTarget.bucket)
                    .key(objectKey)
                    .contentType(ZIP_CONTENT_TYPE)
                    .build()

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(cleanedZipBytes))

            UploadResponse(
                bucket = storageTarget.bucket,
                key = objectKey,
                sizeBytes = cleanedZipBytes.size.toLong(),
            )
        }
    }

    private fun extractSurnameAndName(originalFileName: String): String {
        val baseFileName =
            originalFileName
                .substringAfterLast(UNIX_PATH_SEPARATOR)
                .substringAfterLast(WINDOWS_PATH_SEPARATOR)

        val fileNameWithoutExtension =
            baseFileName.substringBeforeLast(EXTENSION_SEPARATOR, baseFileName)

        val nameParts =
            fileNameWithoutExtension
                .trim()
                .split(PERSON_NAME_SEPARATOR_REGEX)
                .filter { it.isNotBlank() }

        if (nameParts.size < 2) {
            throw InvalidUploadException(ERROR_MESSAGE_FILENAME_SHOULD_CONTAIN_SURNAME_AND_NAME)
        }

        val surname = nameParts[0]
        val name = nameParts[1]

        return (surname + UNDERSCORE + name).lowercase(Locale.ROOT)
    }
}
