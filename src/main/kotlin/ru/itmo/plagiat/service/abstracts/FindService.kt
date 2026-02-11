package ru.itmo.plagiat.service.abstracts

import ru.itmo.plagiat.dto.server.FindWorksResponse

interface FindService {
    fun findWorks(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): FindWorksResponse
}
