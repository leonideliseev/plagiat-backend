package ru.itmo.plagiat.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.itmo.plagiat.dto.server.UploadResponse
import ru.itmo.plagiat.service.UploadService

@RestController
@RequestMapping("/api")
class UploadController(
    private val service: UploadService,
) {
    @PostMapping(
        value = ["/upload/{taskName}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun upload(
        @PathVariable taskName: String,
        @RequestPart("files") files: List<MultipartFile>,
    ): List<UploadResponse> = service.uploadTask(taskName =  taskName,
        files = files)
}
