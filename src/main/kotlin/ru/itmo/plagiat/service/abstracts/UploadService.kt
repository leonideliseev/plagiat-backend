package ru.itmo.plagiat.service.abstracts

import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.dto.server.UploadResponse

interface UploadService {
    fun uploadWorks(
        workName: String,
        files: List<MultipartFile>,
        bucketKey: String?,
        prefixKey: String?,
    ): List<UploadResponse>
}
