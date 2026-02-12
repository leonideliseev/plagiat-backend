package ru.itmo.plagiat.client

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.configuration.TemplateCreatorProperties

private const val MULTIPART_FIELD_FILE = "file"
private const val MULTIPART_FIELD_STORAGE_PATH = "storagePath"
private const val DEFAULT_FILE_NAME = "technical-specification.txt"

@Component
class TemplateCreatorClient(
    private val restClient: RestClient,
    private val properties: TemplateCreatorProperties,
) {
    fun createTemplate(
        technicalSpecification: MultipartFile,
        storagePath: String,
    ): String {
        val body = LinkedMultiValueMap<String, Any>()

        val fileResource =
            object : ByteArrayResource(technicalSpecification.bytes) {
                override fun getFilename(): String =
                    technicalSpecification.originalFilename
                        ?: DEFAULT_FILE_NAME
            }

        body.add(MULTIPART_FIELD_FILE, fileResource)
        body.add(MULTIPART_FIELD_STORAGE_PATH, storagePath)

        return restClient
            .post()
            .uri(properties.createUrl())
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .retrieve()
            .body(String::class.java)
            ?: ""
    }
}
