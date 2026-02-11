package ru.itmo.plagiat.service.zip

import org.springframework.stereotype.Component
import ru.itmo.plagiat.dto.exception.InvalidUploadException
import ru.itmo.plagiat.util.ERROR_MESSAGE_CANNOT_READ_NAMES
import ru.itmo.plagiat.util.ERROR_MESSAGE_INVALID_ARCHIVE
import ru.itmo.plagiat.util.ZIP_EXTENSION
import ru.itmo.plagiat.util.ZIP_NAME_CHARSETS
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipException
import java.util.zip.ZipFile

private const val TEMP_ZIP_PREFIX = "plagiat-ai-"
private const val TEMP_ZIP_SUFFIX = ZIP_EXTENSION
private const val ZIP_ENTRY_SEPARATOR = '/'

@Component
class ZipUnpacker {
    fun unpack(
        zipBytes: ByteArray,
        destinationDirectory: Path,
    ) {
        val tempZipFile = Files.createTempFile(TEMP_ZIP_PREFIX, TEMP_ZIP_SUFFIX)

        try {
            Files.write(tempZipFile, zipBytes)

            var lastException: Exception? = null

            for (charset in ZIP_NAME_CHARSETS) {
                try {
                    unzipWithCharset(
                        zipFilePath = tempZipFile,
                        destinationDirectory = destinationDirectory,
                        charset = charset,
                    )
                    return
                } catch (exception: IllegalArgumentException) {
                    lastException = exception
                } catch (exception: java.nio.charset.MalformedInputException) {
                    lastException = exception
                } catch (exception: ZipException) {
                    lastException = exception
                }
            }

            throw InvalidUploadException(ERROR_MESSAGE_CANNOT_READ_NAMES).also {
                if (lastException != null) it.addSuppressed(lastException)
            }
        } finally {
            Files.deleteIfExists(tempZipFile)
        }
    }

    private fun unzipWithCharset(
        zipFilePath: Path,
        destinationDirectory: Path,
        charset: Charset,
    ) {
        ZipFile(zipFilePath.toFile(), charset).use { zipFile ->
            val entries = zipFile.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name.replace('\\', ZIP_ENTRY_SEPARATOR)

                if (entryName.isBlank()) continue
                if (entryName.startsWith("/")) continue

                val targetPath = destinationDirectory.resolve(entryName).normalize()
                if (!targetPath.startsWith(destinationDirectory)) {
                    throw InvalidUploadException(ERROR_MESSAGE_INVALID_ARCHIVE)
                }

                if (entry.isDirectory) {
                    Files.createDirectories(targetPath)
                } else {
                    Files.createDirectories(targetPath.parent)
                    zipFile.getInputStream(entry).use { input ->
                        Files.newOutputStream(targetPath).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }
}
