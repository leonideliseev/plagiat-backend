package ru.itmo.plagiat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class PlagiatApplication

fun main(args: Array<String>) {
    runApplication<PlagiatApplication>(*args)
}
