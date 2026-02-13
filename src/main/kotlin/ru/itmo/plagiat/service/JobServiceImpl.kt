package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import ru.itmo.plagiat.client.FilesAnalyzerClient
import ru.itmo.plagiat.client.PlagiatCheckerClient
import ru.itmo.plagiat.dto.server.GetAiCheckJobResponse
import ru.itmo.plagiat.dto.server.GetPlagiatCheckJobResponse
import ru.itmo.plagiat.service.abstracts.JobService

@Service
class JobServiceImpl(
    private val filesAnalyzerClient: FilesAnalyzerClient,
    private val plagiatCheckerClient: PlagiatCheckerClient,
) : JobService {
    override fun getAiCheckJob(jobId: String): GetAiCheckJobResponse = filesAnalyzerClient.getJob(jobId = jobId)

    override fun getPlagiatCheckJob(jobId: String): GetPlagiatCheckJobResponse = plagiatCheckerClient.getJob(jobId = jobId)
}
