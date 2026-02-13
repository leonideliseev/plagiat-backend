package ru.itmo.plagiat.service.abstracts

import ru.itmo.plagiat.dto.server.GetAiCheckJobResponse
import ru.itmo.plagiat.dto.server.GetPlagiatCheckJobResponse

interface JobService {
    fun getAiCheckJob(jobId: String): GetAiCheckJobResponse

    fun getPlagiatCheckJob(jobId: String): GetPlagiatCheckJobResponse
}
