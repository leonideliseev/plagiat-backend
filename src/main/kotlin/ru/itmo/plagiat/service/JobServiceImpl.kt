package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import ru.itmo.plagiat.client.FilesAnalyzerClient
import ru.itmo.plagiat.service.abstracts.JobService

@Service
class JobServiceImpl(
    private val filesAnalyzerClient: FilesAnalyzerClient,
) : JobService {
    override fun getJob(jobId: String): String = filesAnalyzerClient.getJob(jobId = jobId)
}
