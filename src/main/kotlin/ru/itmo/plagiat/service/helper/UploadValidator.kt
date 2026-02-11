package ru.itmo.plagiat.service.helper

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.dto.exception.InvalidUploadException
import ru.itmo.plagiat.util.ERROR_MESSAGE_FILE_EMPTY
import ru.itmo.plagiat.util.ERROR_MESSAGE_NOT_ZIP
import ru.itmo.plagiat.util.ERROR_MESSAGE_NO_FILENAME
import ru.itmo.plagiat.util.ZIP_EXTENSION
import java.util.Locale

@Component
class UploadValidator {
    fun validateZip(file: MultipartFile) {
        if (file.isEmpty) throw InvalidUploadException(ERROR_MESSAGE_FILE_EMPTY)

        val originalFileName =
            file.originalFilename
                ?: throw InvalidUploadException(ERROR_MESSAGE_NO_FILENAME)

        val isZipArchive = originalFileName.lowercase(Locale.ROOT).endsWith(ZIP_EXTENSION)
        if (!isZipArchive) throw InvalidUploadException(ERROR_MESSAGE_NOT_ZIP)
    }
}
