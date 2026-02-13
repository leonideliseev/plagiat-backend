package ru.itmo.plagiat.controller.abstracts

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.itmo.plagiat.dto.server.CreateAiCheckJobRequest
import ru.itmo.plagiat.dto.server.CreateAiCheckJobResponse
import ru.itmo.plagiat.dto.server.CreatePlagiatCheckJobRequest
import ru.itmo.plagiat.dto.server.CreatePlagiatCheckJobResponse
import ru.itmo.plagiat.dto.server.GetAiCheckJobResponse
import ru.itmo.plagiat.dto.server.GetPlagiatCheckJobResponse

interface JobApi {
    @GetMapping(
        value = ["/api/v1/ai/check/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getAiCheckJob(
        @PathVariable jobId: String,
    ): GetAiCheckJobResponse

    @GetMapping(
        value = ["/api/v1/plagiat/check/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getPlagiatCheckJob(
        @PathVariable jobId: String,
    ): GetPlagiatCheckJobResponse

    @PostMapping(
        value = ["/api/v1/storage/{bucketKey}/{prefixKey}/{workName}/ai/check/jobs/create"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createAiCheckJob(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: CreateAiCheckJobRequest,
    ): CreateAiCheckJobResponse

    @PostMapping(
        value = ["/storage/{bucketKey}/{prefixKey}/{workName}/plagiat/check/jobs/create"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createPlagiatCheckJob(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: CreatePlagiatCheckJobRequest,
    ): CreatePlagiatCheckJobResponse
}
