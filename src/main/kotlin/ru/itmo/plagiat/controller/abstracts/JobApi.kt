package ru.itmo.plagiat.controller.abstracts

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

interface JobApi {
    @GetMapping(
        value = ["/api/v1/jobs/{jobId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getJob(
        @PathVariable jobId: String,
    ): String
}
