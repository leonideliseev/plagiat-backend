package ru.itmo.plagiat.service.abstracts

interface JobService {
    fun getJob(jobId: String): String
}
