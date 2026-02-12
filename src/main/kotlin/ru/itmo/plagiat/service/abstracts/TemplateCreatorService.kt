package ru.itmo.plagiat.service.abstracts

import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.dto.client.TemplateCreateResponse

interface TemplateCreatorService {
    fun createTemplate(
        technicalSpecification: MultipartFile,
        bucketKey: String,
        prefixKey: String,
        workName: String,
    ): TemplateCreateResponse
}
