package ru.itmo.plagiat.client

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.itmo.plagiat.configuration.PlagiatCheckerProperties
import ru.itmo.plagiat.dto.client.PlagiatCheckerRequest
import ru.itmo.plagiat.dto.server.GetPlagiatCheckJobResponse

@Component
class PlagiatCheckerClient(
    private val restClient: RestClient,
    private val plagiatCheckerProperties: PlagiatCheckerProperties,
) {
    fun check(request: PlagiatCheckerRequest) {
        restClient
            .post()
            .uri(plagiatCheckerProperties.checkUrl())
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    fun getJob(jobId: String): GetPlagiatCheckJobResponse =
        restClient
            .get()
            .uri(plagiatCheckerProperties.jobUrl(jobId))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(GetPlagiatCheckJobResponse::class.java)!!
}
