package ru.itmo.plagiat.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.plagiat.controller.abstracts.JobApi
import ru.itmo.plagiat.service.abstracts.JobService

@RestController
@RequestMapping("/api/v1")
class JobController(
    private val jobService: JobService,
) : JobApi {
    @GetMapping(
        value = ["/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    override fun getJob(
        @PathVariable jobId: String,
    ): String = jobService.getJob(jobId = jobId)
}
