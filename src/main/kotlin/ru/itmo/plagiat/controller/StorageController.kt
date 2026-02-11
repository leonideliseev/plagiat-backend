package ru.itmo.plagiat.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.controller.abstracts.StorageApi
import ru.itmo.plagiat.dto.server.CheckAiRequest
import ru.itmo.plagiat.dto.server.CheckAiResponse
import ru.itmo.plagiat.dto.server.FindWorksRequest
import ru.itmo.plagiat.dto.server.FindWorksResponse
import ru.itmo.plagiat.dto.server.UploadResponse
import ru.itmo.plagiat.service.AiCheckService
import ru.itmo.plagiat.service.abstracts.FindService
import ru.itmo.plagiat.service.abstracts.UploadService

@RestController
@RequestMapping("/api/storage")
class StorageController(
    private val uploadService: UploadService,
    private val findService: FindService,
    private val aiCheckService: AiCheckService,
) : StorageApi {
    @PostMapping(
        value = ["/{bucketKey}/{prefixKey}/{workName}/upload"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun upload(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestPart("files") files: List<MultipartFile>,
    ): List<UploadResponse> =
        uploadService.uploadWorks(
            workName = workName,
            files = files,
            bucketKey = bucketKey,
            prefixKey = prefixKey,
        )

    @PostMapping(
        value = ["/{bucketKey}/{prefixKey}/{workName}/find"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun findWorks(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: FindWorksRequest,
    ): FindWorksResponse =
        findService.findWorks(
            bucketKey = bucketKey,
            prefixKey = prefixKey,
            workName = workName,
            fileNameQueries = request.fileNameQueries,
        )

    @PostMapping(
        value = ["/{bucketKey}/{prefixKey}/{workName}/ai-check"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun checkAi(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: CheckAiRequest,
    ): CheckAiResponse =
        aiCheckService.aiCheck(
            bucketKey = bucketKey,
            prefixKey = prefixKey,
            workName = workName,
            fileNameQueries = request.fileNameQueries,
        )
}
