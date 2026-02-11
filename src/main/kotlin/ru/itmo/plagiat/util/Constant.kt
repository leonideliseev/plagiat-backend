package ru.itmo.plagiat.util

import java.nio.charset.Charset

const val S3_KEY_SEPARATOR = "/"

const val ZIP_EXTENSION = ".zip"
const val ZIP_CONTENT_TYPE = "application/zip"
val ZIP_NAME_CHARSETS: List<Charset> =
    listOf(
        Charsets.UTF_8,
        Charset.forName("CP866"),
        Charset.forName("windows-1251"),
    )
