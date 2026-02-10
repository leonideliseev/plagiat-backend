package ru.itmo.plagiat.service.helper

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.dto.exception.InvalidUploadException

@Component
class UploadValidator {
    fun validateZip(file: MultipartFile) {
        if (file.isEmpty) throw InvalidUploadException("Файл пустой")

        val name = file.originalFilename ?: throw InvalidUploadException("Нет имени файла")
        if (!name.lowercase().endsWith(".zip")) throw InvalidUploadException("Нужен zip архив")
    }
}
