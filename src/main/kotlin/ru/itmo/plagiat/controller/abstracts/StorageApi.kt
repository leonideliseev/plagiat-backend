package ru.itmo.plagiat.controller.abstracts

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.dto.client.TemplateCreateResponse
import ru.itmo.plagiat.dto.server.FindWorksRequest
import ru.itmo.plagiat.dto.server.FindWorksResponse
import ru.itmo.plagiat.dto.server.UploadResponse

interface StorageApi {
    @PostMapping(
        value = ["/api/v1/storage/{bucketKey}/{prefixKey}/{workName}/upload"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun upload(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestPart("files") files: List<MultipartFile>,
    ): List<UploadResponse>

    @PostMapping(
        value = ["/api/v1/storage/{bucketKey}/{prefixKey}/{workName}/find"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun findWorks(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: FindWorksRequest,
    ): FindWorksResponse

    @PostMapping(
        value = ["/api/v1/storage/{bucketKey}/{prefixKey}/{workName}/template/create"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createTemplate(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestPart("file") file: MultipartFile,
    ): TemplateCreateResponse
}
