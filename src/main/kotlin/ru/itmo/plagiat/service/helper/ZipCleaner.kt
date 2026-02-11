@file:Suppress("ktlint:standard:no-wildcard-imports")

package ru.itmo.plagiat.service.helper

import org.springframework.stereotype.Component
import ru.itmo.plagiat.configuration.ZipCleanerProperties
import ru.itmo.plagiat.dto.exception.InvalidUploadException
import ru.itmo.plagiat.util.ERROR_MESSAGE_CANNOT_READ_NAMES
import ru.itmo.plagiat.util.ERROR_MESSAGE_INVALID_ARCHIVE
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.Comparator
import kotlin.io.path.extension

private const val CLEAN_DIR_PREFIX = "plagiat-clean-"
private const val UPLOAD_ZIP_PREFIX = "plagiat-upload-"
private const val ZIP_SUFFIX = ".zip"

private const val ENTRY_SEPARATOR = '/'
private const val WINDOWS_SEPARATOR = '\\'
private const val ABSOLUTE_PATH_PREFIX = "/"

private const val MAC_RESOURCE_FORK_PREFIX = "._"
private const val BACKUP_FILE_SUFFIX = "~"
private const val MAC_ICON_FILE_NAME = "icon\r"

private val ZIP_NAME_CHARSETS: List<Charset> =
    listOf(
        Charsets.UTF_8,
        Charset.forName("CP866"),
        Charset.forName("windows-1251"),
    )

@Component
class ZipCleaner(
    private val properties: ZipCleanerProperties,
) {
    fun cleanZip(zipBytes: ByteArray): ByteArray {
        val tempDirectory = Files.createTempDirectory(CLEAN_DIR_PREFIX)

        try {
            unzipSafely(zipBytes, tempDirectory)
            deleteTrash(tempDirectory)

            if (properties.keep.onlyAllowed) {
                keepOnlyAllowedFiles(tempDirectory)
            }

            return zipDirectory(tempDirectory)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    private fun unzipSafely(
        zipBytes: ByteArray,
        destinationDirectory: Path,
    ) {
        val tempZipFile = Files.createTempFile(UPLOAD_ZIP_PREFIX, ZIP_SUFFIX)

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
                if (lastException != null) {
                    it.addSuppressed(lastException)
                }
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
                val zipEntry = entries.nextElement()
                val normalizedEntryName = normalizeEntryName(zipEntry.name)

                if (shouldSkipEntry(normalizedEntryName)) continue

                val targetPath = destinationDirectory.resolve(normalizedEntryName).normalize()
                if (!targetPath.startsWith(destinationDirectory)) {
                    throw InvalidUploadException(ERROR_MESSAGE_INVALID_ARCHIVE)
                }

                if (zipEntry.isDirectory) {
                    Files.createDirectories(targetPath)
                } else {
                    Files.createDirectories(targetPath.parent)
                    zipFile.getInputStream(zipEntry).use { inputStream ->
                        Files.newOutputStream(targetPath).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
        }
    }

    private fun normalizeEntryName(entryName: String): String = entryName.replace(WINDOWS_SEPARATOR, ENTRY_SEPARATOR)

    private fun shouldSkipEntry(entryName: String): Boolean = entryName.isBlank() || entryName.startsWith(ABSOLUTE_PATH_PREFIX)

    private fun deleteTrash(rootDirectory: Path) {
        val trashDirectoryNames = properties.delete.dirs.asLowercaseSet()
        val trashFileNames = properties.delete.files.asLowercaseSet()
        val trashExtensions = properties.delete.extensions.asLowercaseSet()

        walkDeepFirst(rootDirectory) { path ->
            val fileNameLowercase = path.fileName?.toString()?.lowercase(Locale.ROOT) ?: return@walkDeepFirst

            if (Files.isDirectory(path)) {
                if (fileNameLowercase in trashDirectoryNames) {
                    path.toFile().deleteRecursively()
                }
                return@walkDeepFirst
            }

            if (fileNameLowercase in trashFileNames) {
                Files.deleteIfExists(path)
                return@walkDeepFirst
            }

            if (isKnownTrashFile(fileNameLowercase)) {
                Files.deleteIfExists(path)
                return@walkDeepFirst
            }

            val extensionLowercase = path.extension.lowercase(Locale.ROOT)
            if (extensionLowercase in trashExtensions) {
                Files.deleteIfExists(path)
            }
        }

        deleteEmptyDirectories(rootDirectory)
    }

    private fun isKnownTrashFile(fileNameLowercase: String): Boolean =
        fileNameLowercase.startsWith(MAC_RESOURCE_FORK_PREFIX) ||
            fileNameLowercase.endsWith(BACKUP_FILE_SUFFIX) ||
            fileNameLowercase == MAC_ICON_FILE_NAME

    private fun keepOnlyAllowedFiles(rootDirectory: Path) {
        val allowedExtensions = properties.keep.allowedExtensions.asLowercaseSet()
        val specialStemNames = properties.keep.specialNames.asLowercaseSet()

        walkDeepFirst(rootDirectory) { path ->
            if (Files.isDirectory(path)) return@walkDeepFirst

            val baseNameLowercase = path.fileName?.toString()?.lowercase(Locale.ROOT) ?: return@walkDeepFirst
            val extensionLowercase = path.extension.lowercase(Locale.ROOT)
            val stemLowercase = baseNameLowercase.substringBeforeLast('.', baseNameLowercase)

            val keepByStemName = stemLowercase in specialStemNames
            val keepByExtension = extensionLowercase in allowedExtensions

            if (!keepByExtension && !keepByStemName) {
                Files.deleteIfExists(path)
            }
        }

        deleteEmptyDirectories(rootDirectory)
    }

    private fun deleteEmptyDirectories(rootDirectory: Path) {
        walkDeepFirst(rootDirectory) { path ->
            if (!Files.isDirectory(path) || path == rootDirectory) return@walkDeepFirst

            val hasChildren = Files.list(path).use { it.findAny().isPresent }
            if (!hasChildren) Files.deleteIfExists(path)
        }
    }

    private fun zipDirectory(rootDirectory: Path): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()

        ZipOutputStream(byteArrayOutputStream).use { zipOutputStream ->
            Files.walk(rootDirectory).use { stream ->
                stream
                    .filter { Files.isRegularFile(it) }
                    .forEach { filePath ->
                        val relativeName =
                            rootDirectory
                                .relativize(filePath)
                                .toString()
                                .replace(WINDOWS_SEPARATOR, ENTRY_SEPARATOR)

                        zipOutputStream.putNextEntry(ZipEntry(relativeName))
                        Files.newInputStream(filePath).use { inputStream ->
                            inputStream.copyTo(zipOutputStream)
                        }
                        zipOutputStream.closeEntry()
                    }
            }
        }

        return byteArrayOutputStream.toByteArray()
    }

    private fun walkDeepFirst(
        rootDirectory: Path,
        action: (Path) -> Unit,
    ) {
        Files.walk(rootDirectory).use { stream ->
            stream
                .sorted(DEEP_FIRST_SORT)
                .forEach(action)
        }
    }
}

private val DEEP_FIRST_SORT: Comparator<Path> =
    Comparator.comparingInt<Path> { it.toString().length }.reversed()

private fun List<String>.asLowercaseSet(): Set<String> =
    asSequence()
        .map { it.lowercase(Locale.ROOT) }
        .toSet()
