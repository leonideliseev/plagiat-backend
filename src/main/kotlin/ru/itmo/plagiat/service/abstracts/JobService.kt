package ru.itmo.plagiat.service.abstracts

import ru.itmo.plagiat.dto.server.GetAiCheckTaskResponse

interface JobService {
    fun getJob(jobId: String): GetAiCheckTaskResponse
}
