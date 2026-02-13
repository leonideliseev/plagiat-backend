package ru.itmo.plagiat.service.abstracts

import ru.itmo.plagiat.dto.server.CreateAiCheckTaskResponse

interface AiCheckService {
    fun createJob(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): CreateAiCheckTaskResponse
}
