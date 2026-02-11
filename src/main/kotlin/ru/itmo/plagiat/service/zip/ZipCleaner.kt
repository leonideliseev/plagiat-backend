package ru.itmo.plagiat.service.zip

import org.springframework.stereotype.Component
import ru.itmo.plagiat.configuration.ZipCleanerProperties
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.extension

private const val TEMP_DIRECTORY_PREFIX = "plagiat-clean-"
private const val ZIP_ENTRY_SEPARATOR = '/'
private const val ICON_CR_NAME = "icon\r"
private const val APPLE_DOUBLE_PREFIX = "._"
private const val BACKUP_SUFFIX = "~"

@Component
class ZipCleaner(
    private val properties: ZipCleanerProperties,
    private val zipUnpacker: ZipUnpacker,
) {
    fun cleanZip(inputZip: ByteArray): ByteArray {
        val tempDirectory = Files.createTempDirectory(TEMP_DIRECTORY_PREFIX)

        try {
            zipUnpacker.unpack(
                zipBytes = inputZip,
                destinationDirectory = tempDirectory,
            )

            deleteTrash(tempDirectory)

            if (properties.keep.onlyAllowed) {
                keepOnlyAllowedFiles(tempDirectory)
            }

            return zipDirectory(tempDirectory)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    private fun deleteTrash(root: Path) {
        val trashDirectories =
            properties.delete.dirs
                .asSequence()
                .map { it.lowercase() }
                .toSet()

        val trashFiles =
            properties.delete.files
                .asSequence()
                .map { it.lowercase() }
                .toSet()

        val trashExtensions =
            properties.delete.extensions
                .asSequence()
                .map { it.lowercase() }
                .toSet()

        Files.walk(root).use { stream ->
            stream
                .sorted(Comparator.comparingInt<Path> { it.toString().length }.reversed())
                .forEach { path ->
                    val nameLowercase = path.fileName?.toString()?.lowercase() ?: return@forEach

                    if (Files.isDirectory(path)) {
                        if (nameLowercase in trashDirectories) {
                            path.toFile().deleteRecursively()
                        }
                        return@forEach
                    }

                    if (nameLowercase in trashFiles) {
                        Files.deleteIfExists(path)
                        return@forEach
                    }

                    if (nameLowercase.startsWith(APPLE_DOUBLE_PREFIX) ||
                        nameLowercase.endsWith(BACKUP_SUFFIX) ||
                        nameLowercase == ICON_CR_NAME
                    ) {
                        Files.deleteIfExists(path)
                        return@forEach
                    }

                    val extensionLowercase = path.extension.lowercase()
                    if (extensionLowercase in trashExtensions) {
                        Files.deleteIfExists(path)
                        return@forEach
                    }
                }
        }

        deleteEmptyDirectories(root)
    }

    private fun keepOnlyAllowedFiles(root: Path) {
        val allowedExtensions =
            properties.keep.allowedExtensions
                .asSequence()
                .map { it.lowercase() }
                .toSet()

        val specialNames =
            properties.keep.specialNames
                .asSequence()
                .map { it.lowercase() }
                .toSet()

        Files.walk(root).use { stream ->
            stream
                .sorted(Comparator.comparingInt<Path> { it.toString().length }.reversed())
                .forEach { path ->
                    if (Files.isDirectory(path)) return@forEach

                    val fileNameLowercase = path.fileName?.toString()?.lowercase() ?: return@forEach
                    val extensionLowercase = path.extension.lowercase()
                    val stemLowercase = fileNameLowercase.substringBeforeLast('.', fileNameLowercase)

                    val keepByName = stemLowercase in specialNames
                    val keepByExtension = extensionLowercase in allowedExtensions
                    val keep = keepByExtension || keepByName

                    if (!keep) Files.deleteIfExists(path)
                }
        }

        deleteEmptyDirectories(root)
    }

    private fun deleteEmptyDirectories(root: Path) {
        Files.walk(root).use { stream ->
            stream
                .sorted(Comparator.comparingInt<Path> { it.toString().length }.reversed())
                .forEach { path ->
                    if (!Files.isDirectory(path) || path == root) return@forEach

                    val hasChildren = Files.list(path).use { it.findAny().isPresent }
                    if (!hasChildren) Files.deleteIfExists(path)
                }
        }
    }

    private fun zipDirectory(root: Path): ByteArray {
        val outputStream = ByteArrayOutputStream()

        ZipOutputStream(outputStream).use { zipOutputStream ->
            Files.walk(root).use { stream ->
                stream
                    .filter { Files.isRegularFile(it) }
                    .forEach { filePath ->
                        val relativePath =
                            root
                                .relativize(filePath)
                                .toString()
                                .replace('\\', ZIP_ENTRY_SEPARATOR)

                        zipOutputStream.putNextEntry(ZipEntry(relativePath))
                        Files.newInputStream(filePath).use { input -> input.copyTo(zipOutputStream) }
                        zipOutputStream.closeEntry()
                    }
            }
        }

        return outputStream.toByteArray()
    }
}
