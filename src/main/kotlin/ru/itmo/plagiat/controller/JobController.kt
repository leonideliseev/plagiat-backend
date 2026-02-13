package ru.itmo.plagiat.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.plagiat.controller.abstracts.JobApi
import ru.itmo.plagiat.dto.server.CheckAiRequest
import ru.itmo.plagiat.dto.server.CheckAiResponse
import ru.itmo.plagiat.service.AiCheckServiceImpl
import ru.itmo.plagiat.service.abstracts.JobService

@RestController
@RequestMapping("/api/v1")
class JobController(
    private val jobService: JobService,
    private val aiCheckService: AiCheckServiceImpl,
) : JobApi {
    @GetMapping(
        value = ["/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun getJob(
        @PathVariable jobId: String,
    ): String = jobService.getJob(jobId = jobId)

    @PostMapping(
        value = ["/storage/{bucketKey}/{prefixKey}/{workName}/ai/check/jobs/create"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun createAiCheckJob(
        @PathVariable bucketKey: String,
        @PathVariable prefixKey: String,
        @PathVariable workName: String,
        @RequestBody request: CheckAiRequest,
    ): CheckAiResponse =
        aiCheckService.createJob(
            bucketKey = bucketKey,
            prefixKey = prefixKey,
            workName = workName,
            fileNameQueries = request.fileNameQueries,
        )
}
