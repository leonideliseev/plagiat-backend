package ru.itmo.plagiat.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.ai-detection.selection")
data class AiDetectionSelectionProperties(
    val totalFiles: Int = 7,
    val rules: List<FolderSelectionRule> = emptyList(),
    val reroll: RerollPolicy = RerollPolicy(),
    val score: ScorePolicy = ScorePolicy(),
) {
    data class FolderSelectionRule(
        val name: String,
        val pathContainsAny: List<String> = emptyList(),
        val extensionsAny: List<String> = emptyList(),
        val takeLargest: Int = 0,
        val takeRandom: Int = 0,
    )

    data class RerollPolicy(
        val maxAttempts: Int = 20,
        val minLines: Int = 60,
        val minBytes: Long = 1500,
        val maxLines: Int = 2000,
        val maxBytes: Long = 200000,
        val skipMinifiedJs: Boolean = true,
        val minified: MinifiedPolicy = MinifiedPolicy(),
    ) {
        data class MinifiedPolicy(
            val maxAverageLineLength: Int = 220,
            val maxLongLineRatio: Double = 0.35,
            val longLineThreshold: Int = 300,
        )
    }

    data class ScorePolicy(
        val weightBytes: Double = 0.35,
        val weightLines: Double = 0.65,
    )
}
