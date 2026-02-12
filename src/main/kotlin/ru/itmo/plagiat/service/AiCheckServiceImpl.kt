package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import ru.itmo.plagiat.client.FilesAnalyzerClient
import ru.itmo.plagiat.dto.client.AnalyzeFile
import ru.itmo.plagiat.dto.client.FilesAnalyzeRequest
import ru.itmo.plagiat.dto.server.CheckAiResponse
import ru.itmo.plagiat.dto.server.SelectedArchiveFile
import ru.itmo.plagiat.service.abstracts.AiCheckService
import ru.itmo.plagiat.service.abstracts.FindService
import ru.itmo.plagiat.service.helper.NanoIdGenerator
import ru.itmo.plagiat.service.selection.AiArchiveFilePicker
import ru.itmo.plagiat.service.storage.S3ObjectDownloader
import ru.itmo.plagiat.service.zip.ZipUnpacker
import ru.itmo.plagiat.util.ZIP_EXTENSION
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

private const val TEMP_DIRECTORY_PREFIX = "plagiat-ai-check-"
private const val EMPTY_JOB_ID = "none"
private val DEFAULT_FILE_CHARSET: Charset = Charsets.UTF_8

@Service
class AiCheckServiceImpl(
    private val findService: FindService,
    private val s3ObjectDownloader: S3ObjectDownloader,
    private val zipUnpacker: ZipUnpacker,
    private val aiArchiveFilePicker: AiArchiveFilePicker,
    private val nanoIdGenerator: NanoIdGenerator,
    private val filesAnalyzerClient: FilesAnalyzerClient,
) : AiCheckService {
    override fun createJob(
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
                jobId = EMPTY_JOB_ID,
                bucket = bucket,
                archiveKey = null,
                selectedFiles = emptyList(),
            )
        }

        val jobId = nanoIdGenerator.randomId()
        val zipBytes = s3ObjectDownloader.downloadBytes(bucket = bucket, key = firstZipKey)

        val tempDirectory = Files.createTempDirectory(TEMP_DIRECTORY_PREFIX)
        try {
            zipUnpacker.unpack(
                zipBytes = zipBytes,
                destinationDirectory = tempDirectory,
            )

            val pickedFiles = aiArchiveFilePicker.pickFiles(rootDirectory = tempDirectory)

            sendToAnalyzer(
                jobId = jobId,
                rootDirectory = tempDirectory,
                pickedFiles = pickedFiles,
            )

            return CheckAiResponse(
                jobId = jobId,
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

    private fun sendToAnalyzer(
        jobId: String,
        rootDirectory: Path,
        pickedFiles: List<ru.itmo.plagiat.service.selection.PickedArchiveFile>,
    ) {
        val analyzeRequest =
            FilesAnalyzeRequest(
                id = jobId,
                files =
                    pickedFiles.map { picked ->
                        val absolutePath = rootDirectory.resolve(picked.relativePath)
                        val code = readFileAsText(absolutePath)

                        AnalyzeFile(
                            name = picked.relativePath,
                            code = code,
                        )
                    },
            )

        filesAnalyzerClient.analyze(analyzeRequest)
    }

    private fun readFileAsText(path: Path): String =
        try {
            Files.readString(path, DEFAULT_FILE_CHARSET)
        } catch (exception: Exception) {
            Files.readString(path, Charsets.ISO_8859_1)
        }
}
