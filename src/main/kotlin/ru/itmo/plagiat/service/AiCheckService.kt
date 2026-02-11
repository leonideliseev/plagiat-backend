package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import ru.itmo.plagiat.dto.server.CheckAiResponse
import ru.itmo.plagiat.dto.server.SelectedArchiveFile
import ru.itmo.plagiat.service.abstracts.FindService
import ru.itmo.plagiat.service.selection.AiArchiveFilePicker
import ru.itmo.plagiat.service.storage.S3ObjectDownloader
import ru.itmo.plagiat.service.zip.ZipUnpacker
import ru.itmo.plagiat.util.ZIP_EXTENSION
import java.nio.file.Files
import java.util.Locale

private const val TEMP_DIRECTORY_PREFIX = "plagiat-ai-check-"

@Service
class AiCheckService(
    private val findService: FindService,
    private val s3ObjectDownloader: S3ObjectDownloader,
    private val zipUnpacker: ZipUnpacker,
    private val aiArchiveFilePicker: AiArchiveFilePicker,
) {
    fun aiCheck(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): CheckAiResponse {
        val findResponse =
            findService.findWorks(
                bucketKey = bucketKey,
                prefixKey = prefixKey,
                workName = workName,
                fileNameQueries = fileNameQueries,
            )

        val bucket = findResponse.bucket

        val firstZipKey =
            findResponse.keys.firstOrNull { key ->
                key.lowercase(Locale.ROOT).endsWith(ZIP_EXTENSION)
            }

        if (firstZipKey == null) {
            return CheckAiResponse(
                bucket = bucket,
                archiveKey = null,
                selectedFiles = emptyList(),
            )
        }

        val zipBytes = s3ObjectDownloader.downloadBytes(bucket = bucket, key = firstZipKey)

        val tempDirectory = Files.createTempDirectory(TEMP_DIRECTORY_PREFIX)
        try {
            zipUnpacker.unpack(
                zipBytes = zipBytes,
                destinationDirectory = tempDirectory,
            )

            val pickedFiles = aiArchiveFilePicker.pickFiles(rootDirectory = tempDirectory)

            return CheckAiResponse(
                bucket = bucket,
                archiveKey = firstZipKey,
                selectedFiles =
                    pickedFiles.map { picked ->
                        SelectedArchiveFile(
                            pathInArchive = picked.relativePath,
                            sizeBytes = picked.sizeBytes,
                            linesCount = picked.linesCount,
                        )
                    },
            )
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }
}
