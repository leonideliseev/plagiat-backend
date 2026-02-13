package ru.itmo.plagiat.controller.abstracts

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.itmo.plagiat.dto.server.CreateAiCheckTaskRequest
import ru.itmo.plagiat.dto.server.CreateAiCheckTaskResponse
import ru.itmo.plagiat.dto.server.GetAiCheckTaskResponse

interface JobApi {
    @GetMapping(
        value = ["/api/v1/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getJob(
        @PathVariable jobId: String,
    ): GetAiCheckTaskResponse

    @PostMapping(
        value = ["/api/v1/storage/{bucketKey}/{prefixKey}/{workName}/ai/check/jobs/create"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createAiCheckJob(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: CreateAiCheckTaskRequest,
    ): CreateAiCheckTaskResponse
}
