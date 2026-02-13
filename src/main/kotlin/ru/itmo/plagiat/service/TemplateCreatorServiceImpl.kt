package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.client.TemplateCreatorClient
import ru.itmo.plagiat.dto.client.TemplateCreateResponse
import ru.itmo.plagiat.dto.exception.InvalidUploadException
import ru.itmo.plagiat.service.abstracts.TemplateCreatorService
import ru.itmo.plagiat.service.storage.StorageSelector

private const val PATH_SEPARATOR = "/"

@Service
class TemplateCreatorServiceImpl(
    private val storageSelector: StorageSelector,
    private val aiTemplateCreatorClient: TemplateCreatorClient,
) : TemplateCreatorService {
    override fun createTemplate(
        technicalSpecification: MultipartFile,
        bucketKey: String,
        prefixKey: String,
        workName: String,
    ): TemplateCreateResponse {
        storageSelector.select(bucketKey = bucketKey, prefixKey = prefixKey)

        val normalizedWorkName = normalizePathPart(workName, "workName")

        val storagePath =
            listOf(bucketKey, prefixKey, normalizedWorkName).joinToString(PATH_SEPARATOR) { it.trim() }

        aiTemplateCreatorClient.createTemplate(
            technicalSpecification = technicalSpecification,
            storagePath = storagePath,
        )

        return TemplateCreateResponse(success = true)
    }

    private fun normalizePathPart(
        value: String,
        fieldName: String,
    ): String {
        val cleaned = value.trim().trim(PATH_SEPARATOR.single())
        if (cleaned.isBlank()) throw InvalidUploadException("$fieldName пустой")
        if (cleaned == "." || cleaned == "..") throw InvalidUploadException("$fieldName недопустим")
        if (cleaned.contains(PATH_SEPARATOR)) throw InvalidUploadException("$fieldName не должен содержать /")
        return cleaned
    }
}
