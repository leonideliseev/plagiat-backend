package ru.itmo.plagiat.service.selection

import org.springframework.stereotype.Component
import ru.itmo.plagiat.configuration.AiDetectionSelectionProperties
import ru.itmo.plagiat.configuration.ZipCleanerProperties
import java.nio.file.Files
import java.nio.file.Path
import java.security.SecureRandom
import java.util.Locale
import kotlin.io.path.extension
import kotlin.math.ln
import kotlin.streams.asSequence

private const val PATH_SEPARATOR = "/"
private const val DEFAULT_ROOT_RELATIVE_PATH = ""

@Component
class AiArchiveFilePicker(
    private val aiSelectionProperties: AiDetectionSelectionProperties,
    private val zipCleanerProperties: ZipCleanerProperties,
) {
    private val random = SecureRandom()

    fun pickFiles(rootDirectory: Path): List<PickedArchiveFile> {
        val allowedExtensions = resolveAllowedExtensions()
        val candidates = loadCandidates(rootDirectory, allowedExtensions)

        if (candidates.isEmpty()) return emptyList()

        val selected = LinkedHashMap<String, CandidateFile>()

        for (rule in aiSelectionProperties.rules) {
            val ruleCandidates = candidates.filter { matchesRule(it, rule) }

            selectLargest(ruleCandidates, rule.takeLargest, selected)
            selectRandomWithReroll(ruleCandidates, rule.takeRandom, selected)
        }

        val remaining = (aiSelectionProperties.totalFiles - selected.size).coerceAtLeast(0)
        if (remaining > 0) {
            fillByScore(candidates, remaining, selected)
        }

        return selected.values
            .take(aiSelectionProperties.totalFiles)
            .map {
                PickedArchiveFile(
                    relativePath = it.relativePath,
                    sizeBytes = it.sizeBytes,
                    linesCount = it.linesCount,
                )
            }
    }

    private fun resolveAllowedExtensions(): Set<String> {
        val keep = zipCleanerProperties.keep
        if (!keep.onlyAllowed) {
            return emptySet()
        }

        return keep.allowedExtensions
            .asSequence()
            .map { it.lowercase(Locale.ROOT) }
            .toSet()
    }

    private fun loadCandidates(
        rootDirectory: Path,
        allowedExtensions: Set<String>,
    ): List<CandidateFile> {
        val reroll = aiSelectionProperties.reroll

        return Files.walk(rootDirectory).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .asSequence()
                .mapNotNull { filePath ->
                    val extensionLowercase = filePath.extension.lowercase(Locale.ROOT)

                    if (allowedExtensions.isNotEmpty() && extensionLowercase !in allowedExtensions) return@mapNotNull null

                    val sizeBytes = Files.size(filePath)
                    if (sizeBytes < reroll.minBytes) return@mapNotNull null
                    if (sizeBytes > reroll.maxBytes) return@mapNotNull null

                    val metrics = readTextMetrics(filePath)
                    if (metrics.linesCount < reroll.minLines) return@mapNotNull null
                    if (metrics.linesCount > reroll.maxLines) return@mapNotNull null

                    if (reroll.skipMinifiedJs && isMinifiedLike(extensionLowercase, metrics.averageLineLength, metrics.longLineRatio)) {
                        return@mapNotNull null
                    }

                    val relativePath =
                        rootDirectory
                            .relativize(filePath)
                            .toString()
                            .replace('\\', PATH_SEPARATOR.single())
                            .ifBlank { DEFAULT_ROOT_RELATIVE_PATH }

                    CandidateFile(
                        absolutePath = filePath,
                        relativePath = relativePath,
                        extensionLowercase = extensionLowercase,
                        sizeBytes = sizeBytes,
                        linesCount = metrics.linesCount,
                        averageLineLength = metrics.averageLineLength,
                        longLineRatio = metrics.longLineRatio,
                    )
                }.toList()
        }
    }

    private data class TextMetrics(
        val linesCount: Int,
        val averageLineLength: Double,
        val longLineRatio: Double,
    )

    private fun readTextMetrics(filePath: Path): TextMetrics {
        val longLineThreshold = aiSelectionProperties.reroll.minified.longLineThreshold

        Files.newBufferedReader(filePath).use { reader ->
            var linesCount = 0
            var totalLineLength = 0
            var longLinesCount = 0

            while (true) {
                val line = reader.readLine() ?: break
                linesCount++

                val length = line.length
                totalLineLength += length
                if (length >= longLineThreshold) longLinesCount++
            }

            if (linesCount == 0) {
                return TextMetrics(
                    linesCount = 0,
                    averageLineLength = 0.0,
                    longLineRatio = 0.0,
                )
            }

            return TextMetrics(
                linesCount = linesCount,
                averageLineLength = totalLineLength.toDouble() / linesCount.toDouble(),
                longLineRatio = longLinesCount.toDouble() / linesCount.toDouble(),
            )
        }
    }

    private fun isMinifiedLike(
        extensionLowercase: String,
        averageLineLength: Double,
        longLineRatio: Double,
    ): Boolean {
        val isFrontend =
            extensionLowercase == "js" ||
                extensionLowercase == "ts" ||
                extensionLowercase == "jsx" ||
                extensionLowercase == "tsx"

        if (!isFrontend) return false

        val minified = aiSelectionProperties.reroll.minified

        if (averageLineLength > minified.maxAverageLineLength) return true
        if (longLineRatio > minified.maxLongLineRatio) return true

        return false
    }

    private fun matchesRule(
        candidate: CandidateFile,
        rule: AiDetectionSelectionProperties.FolderSelectionRule,
    ): Boolean {
        val pathLowercase = (PATH_SEPARATOR + candidate.relativePath + PATH_SEPARATOR).lowercase(Locale.ROOT)

        val matchesPath =
            if (rule.pathContainsAny.isEmpty()) {
                true
            } else {
                rule.pathContainsAny.any { token -> pathLowercase.contains(token.lowercase(Locale.ROOT)) }
            }

        val matchesExtension =
            if (rule.extensionsAny.isEmpty()) {
                true
            } else {
                rule.extensionsAny.any { ext -> candidate.extensionLowercase == ext.lowercase(Locale.ROOT) }
            }

        return matchesPath && matchesExtension
    }

    private fun selectLargest(
        candidates: List<CandidateFile>,
        takeLargest: Int,
        selected: MutableMap<String, CandidateFile>,
    ) {
        if (takeLargest <= 0) return

        candidates
            .asSequence()
            .filter { it.relativePath !in selected }
            .sortedByDescending { it.sizeBytes }
            .take(takeLargest)
            .forEach { selected[it.relativePath] = it }
    }

    private fun selectRandomWithReroll(
        candidates: List<CandidateFile>,
        takeRandom: Int,
        selected: MutableMap<String, CandidateFile>,
    ) {
        if (takeRandom <= 0) return

        val maxAttempts = aiSelectionProperties.reroll.maxAttempts

        var added = 0
        var attempts = 0

        while (added < takeRandom && attempts < maxAttempts) {
            attempts++

            val pool = candidates.filter { it.relativePath !in selected }
            if (pool.isEmpty()) return

            val picked = pool[random.nextInt(pool.size)]
            selected[picked.relativePath] = picked
            added++
        }
    }

    private fun fillByScore(
        candidates: List<CandidateFile>,
        remainingSlots: Int,
        selected: MutableMap<String, CandidateFile>,
    ) {
        val weightBytes = aiSelectionProperties.score.weightBytes
        val weightLines = aiSelectionProperties.score.weightLines

        val sorted =
            candidates
                .asSequence()
                .filter { it.relativePath !in selected }
                .map { it to score(it, weightBytes, weightLines) }
                .sortedByDescending { it.second }
                .map { it.first }
                .toList()

        sorted.take(remainingSlots).forEach { selected[it.relativePath] = it }
    }

    private fun score(
        candidate: CandidateFile,
        weightBytes: Double,
        weightLines: Double,
    ): Double {
        val normalizedLines = ln(candidate.linesCount.toDouble().coerceAtLeast(1.0) + 1.0)
        val normalizedBytes = ln(candidate.sizeBytes.toDouble().coerceAtLeast(1.0) + 1.0)
        return normalizedLines * weightLines + normalizedBytes * weightBytes
    }
}

data class PickedArchiveFile(
    val relativePath: String,
    val sizeBytes: Long,
    val linesCount: Int,
)

private data class CandidateFile(
    val absolutePath: Path,
    val relativePath: String,
    val extensionLowercase: String,
    val sizeBytes: Long,
    val linesCount: Int,
    val averageLineLength: Double,
    val longLineRatio: Double,
)
