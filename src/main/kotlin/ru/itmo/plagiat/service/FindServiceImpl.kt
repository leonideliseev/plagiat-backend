package ru.itmo.plagiat.service

import org.springframework.stereotype.Service
import ru.itmo.plagiat.dto.server.FindWorksResponse
import ru.itmo.plagiat.service.abstracts.FindService
import ru.itmo.plagiat.service.storage.S3KeyPrefixBuilder
import ru.itmo.plagiat.service.storage.S3KeyQueryFilter
import ru.itmo.plagiat.service.storage.S3ObjectLister
import ru.itmo.plagiat.service.storage.StorageSelector

@Service
class FindServiceImpl(
    private val storageSelector: StorageSelector,
    private val s3KeyPrefixBuilder: S3KeyPrefixBuilder,
    private val s3ObjectLister: S3ObjectLister,
    private val s3KeyQueryFilter: S3KeyQueryFilter,
) : FindService {
    override fun findWorks(
        bucketKey: String,
        prefixKey: String,
        workName: String,
        fileNameQueries: List<String>?,
    ): FindWorksResponse {
        val storageTarget = storageSelector.select(bucketKey = bucketKey, prefixKey = prefixKey)

        val objectKeyPrefix = s3KeyPrefixBuilder.build(prefix = storageTarget.prefix, workName = workName)
        val allKeys = s3ObjectLister.listAllKeys(bucket = storageTarget.bucket, objectKeyPrefix = objectKeyPrefix)
        val filteredKeys = s3KeyQueryFilter.filter(keys = allKeys, fileNameQueries = fileNameQueries)

        return FindWorksResponse(
            bucket = storageTarget.bucket,
            keys = filteredKeys,
        )
    }
}
