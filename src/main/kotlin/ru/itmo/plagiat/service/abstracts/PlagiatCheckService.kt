package ru.itmo.plagiat.service.abstracts

import ru.itmo.plagiat.dto.server.CreatePlagiatCheckJobResponse

interface PlagiatCheckService {
    fun createJob(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): CreatePlagiatCheckJobResponse
}
