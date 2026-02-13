package ru.itmo.plagiat.client

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.itmo.plagiat.configuration.FilesAnalyzerProperties
import ru.itmo.plagiat.dto.client.FilesAnalyzeRequest
import ru.itmo.plagiat.dto.server.GetAiCheckJobResponse

@Component
class FilesAnalyzerClient(
    private val restClient: RestClient,
    private val filesAnalyzerProperties: FilesAnalyzerProperties,
) {
    fun analyze(request: FilesAnalyzeRequest) {
        restClient
            .post()
            .uri(filesAnalyzerProperties.analyzeUrl())
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    fun getJob(jobId: String): GetAiCheckJobResponse =
        restClient
            .get()
            .uri(filesAnalyzerProperties.jobUrl(jobId))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetAiCheckJobResponse::class.java)!!
}
