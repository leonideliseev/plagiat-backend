package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import ru.itmo.plagiat.client.PlagiatCheckerClient
import ru.itmo.plagiat.dto.client.PlagiatCheckerRequest
import ru.itmo.plagiat.dto.server.CreatePlagiatCheckJobResponse
import ru.itmo.plagiat.dto.server.FindWorksResponse
import ru.itmo.plagiat.service.abstracts.FindService
import ru.itmo.plagiat.service.abstracts.PlagiatCheckService
import ru.itmo.plagiat.service.helper.NanoIdGenerator

@Service
class PlagiatCheckServiceImpl(
    private val findService: FindService,
    private val nanoIdGenerator: NanoIdGenerator,
    private val plagiatCheckerClient: PlagiatCheckerClient,
) : PlagiatCheckService {
    override fun createJob(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): CreatePlagiatCheckJobResponse {
        val findWorksResponse: FindWorksResponse =
            findService.findWorks(
                bucketKey = bucketKey,
                prefixKey = prefixKey,
                workName = workName,
                fileNameQueries = fileNameQueries,
            )

        val jobId = nanoIdGenerator.randomId()

        plagiatCheckerClient.check(
            PlagiatCheckerRequest(
                jobId = jobId,
                keys = findWorksResponse.keys,
                bucket = findWorksResponse.bucket,
            ),
        )

        return CreatePlagiatCheckJobResponse(jobId = jobId)
    }
}
