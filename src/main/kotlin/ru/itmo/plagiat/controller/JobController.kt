package ru.itmo.plagiat.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.plagiat.controller.abstracts.JobApi
import ru.itmo.plagiat.dto.server.CreateAiCheckJobRequest
import ru.itmo.plagiat.dto.server.CreateAiCheckJobResponse
import ru.itmo.plagiat.dto.server.CreatePlagiatCheckJobRequest
import ru.itmo.plagiat.dto.server.CreatePlagiatCheckJobResponse
import ru.itmo.plagiat.dto.server.GetAiCheckJobResponse
import ru.itmo.plagiat.dto.server.GetPlagiatCheckJobResponse
import ru.itmo.plagiat.service.AiCheckServiceImpl
import ru.itmo.plagiat.service.abstracts.JobService
import ru.itmo.plagiat.service.abstracts.PlagiatCheckService

@RestController
@RequestMapping("/api/v1")
class JobController(
    private val jobService: JobService,
    private val aiCheckService: AiCheckServiceImpl,
    private val plagiatCheckService: PlagiatCheckService,
) : JobApi {
    @GetMapping(
        value = ["/ai/check/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun getAiCheckJob(
        @PathVariable jobId: String,
    ): GetAiCheckJobResponse = jobService.getAiCheckJob(jobId = jobId)

    @GetMapping(
        value = ["/plagiat/check/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun getPlagiatCheckJob(
        @PathVariable jobId: String,
    ): GetPlagiatCheckJobResponse = jobService.getPlagiatCheckJob(jobId = jobId)

    @PostMapping(
        value = ["/storage/{bucketKey}/{prefixKey}/{workName}/ai/check/jobs/create"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun createAiCheckJob(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: CreateAiCheckJobRequest,
    ): CreateAiCheckJobResponse =
        aiCheckService.createJob(
            bucketKey = bucketKey,
            prefixKey = prefixKey,
            workName = workName,
            fileNameQueries = request.fileNameQueries,
        )

    @PostMapping(
        value = ["/storage/{bucketKey}/{prefixKey}/{workName}/plagiat/check/jobs/create"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun createPlagiatCheckJob(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: CreatePlagiatCheckJobRequest,
    ): CreatePlagiatCheckJobResponse =
        plagiatCheckService.createJob(
            bucketKey = bucketKey,
            prefixKey = prefixKey,
            workName = workName,
            fileNameQueries = request.fileNameQueries,
        )
}
