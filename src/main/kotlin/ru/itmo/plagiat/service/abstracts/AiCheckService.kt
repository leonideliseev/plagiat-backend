package ru.itmo.plagiat.service.abstracts

import ru.itmo.plagiat.dto.server.CheckAiResponse

interface AiCheckService {
    fun createJob(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): CheckAiResponse
}
