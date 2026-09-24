package com.example.data.remote

import android.util.Log
import android.util.Xml
import com.example.data.model.S3AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class S3ListResult(
    val tracks: List<S3AudioTrack>,
    val commonPrefixes: List<String>,
    val isTruncated: Boolean,
    val nextContinuationToken: String?
)

class S3RemoteService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    private val audioExtensions = setOf(
        "mp3", "flac", "wav", "m4a", "aac", "ogg", "opus", "wma", "aiff", "ape"
    )

    /**
     * Lists real objects from an S3 or S3-compatible (Inspur OSS, Aliyun OSS, MinIO) bucket.
     */
    suspend fun listObjectsV2(
        endpoint: String,
        bucketName: String,
        prefix: String,
        delimiter: String = "/",
        region: String = "cn-north-3",
        accessKeyId: String = "",
        secretAccessKey: String = "",
        usePathStyle: Boolean = false,
        useTls: Boolean = true,
        isPublicAccess: Boolean = false
    ): Result<S3ListResult> = withContext(Dispatchers.IO) {
        try {
            val cleanEndpoint = endpoint.trim()
                .removePrefix("https://")
                .removePrefix("http://")
                .trimEnd('/')

            val cleanBucket = bucketName.trim()
            val cleanPrefix = prefix.trim().removePrefix("/")
            val scheme = if (useTls) "https" else "http"

            // Construct Host and URL
            val (host, requestUrl, canonicalUri) = if (usePathStyle) {
                val h = cleanEndpoint
                val url = "$scheme://$h/$cleanBucket/?list-type=2" +
                        (if (cleanPrefix.isNotBlank()) "&prefix=${urlEncode(cleanPrefix)}" else "") +
                        (if (delimiter.isNotBlank()) "&delimiter=${urlEncode(delimiter)}" else "")
                Triple(h, url, "/$cleanBucket/")
            } else {
                val h = if (cleanEndpoint.startsWith("$cleanBucket.")) cleanEndpoint else "$cleanBucket.$cleanEndpoint"
                val url = "$scheme://$h/?list-type=2" +
                        (if (cleanPrefix.isNotBlank()) "&prefix=${urlEncode(cleanPrefix)}" else "") +
                        (if (delimiter.isNotBlank()) "&delimiter=${urlEncode(delimiter)}" else "")
                Triple(h, url, "/")
            }

            val requestBuilder = Request.Builder().url(requestUrl)

            // SigV4 Authentication if credentials provided and not public access
            if (!isPublicAccess && accessKeyId.isNotBlank() && secretAccessKey.isNotBlank()) {
                val queryParams = mutableListOf<Pair<String, String>>()
                if (delimiter.isNotBlank()) queryParams.add("delimiter" to delimiter)
                queryParams.add("list-type" to "2")
                if (cleanPrefix.isNotBlank()) queryParams.add("prefix" to cleanPrefix)
                queryParams.sortBy { it.first }

                val canonicalQueryString = queryParams.joinToString("&") {
                    "${urlEncode(it.first)}=${urlEncode(it.second)}"
                }

                val amzDate = getAmzDate()
                val dateStamp = getDateStamp()

                val canonicalHeaders = "host:$host\nx-amz-content-sha256:UNSIGNED-PAYLOAD\nx-amz-date:$amzDate\n"
                val signedHeaders = "host;x-amz-content-sha256;x-amz-date"
                val payloadHash = "UNSIGNED-PAYLOAD"

                val canonicalRequest = "GET\n$canonicalUri\n$canonicalQueryString\n$canonicalHeaders\n$signedHeaders\n$payloadHash"
                val canonicalRequestHash = sha256Hex(canonicalRequest)

                val algorithm = "AWS4-HMAC-SHA256"
                val credentialScope = "$dateStamp/$region/s3/aws4_request"
                val stringToSign = "$algorithm\n$amzDate\n$credentialScope\n$canonicalRequestHash"

                val signingKey = getSignatureKey(secretAccessKey, dateStamp, region, "s3")
                val signature = hmacSha256Hex(signingKey, stringToSign)

                val authHeader = "$algorithm Credential=$accessKeyId/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

                requestBuilder.header("Host", host)
                requestBuilder.header("x-amz-date", amzDate)
                requestBuilder.header("x-amz-content-sha256", payloadHash)
                requestBuilder.header("Authorization", authHeader)
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                return@withContext Result.failure(
                    Exception("S3 远端请求失败 [HTTP ${response.code}]: ${parseS3ErrorMessage(errorBody).ifBlank { response.message }}")
                )
            }

            val xml = response.body?.string() ?: ""
            val result = parseListObjectsV2Xml(
                xml = xml,
                bucketName = cleanBucket,
                endpoint = cleanEndpoint,
                usePathStyle = usePathStyle,
                useTls = useTls,
                region = region,
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                isPublicAccess = isPublicAccess
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e("S3RemoteService", "Error listing S3 objects", e)
            Result.failure(e)
        }
    }

    /**
     * Parses the S3 XML response from ListObjectsV2.
     */
    private fun parseListObjectsV2Xml(
        xml: String,
        bucketName: String,
        endpoint: String,
        usePathStyle: Boolean,
        useTls: Boolean,
        region: String,
        accessKeyId: String,
        secretAccessKey: String,
        isPublicAccess: Boolean
    ): S3ListResult {
        val tracks = mutableListOf<S3AudioTrack>()
        val commonPrefixes = mutableListOf<String>()
        var isTruncated = false
        var nextContinuationToken: String? = null

        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var currentTag = ""

        // Temporary object fields
        var inContents = false
        var inCommonPrefixes = false
        var currentKey = ""
        var currentSize = 0L
        var currentLastModified = ""
        var currentEtag = ""
        var currentStorageClass = "STANDARD"

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    when (currentTag) {
                        "Contents" -> {
                            inContents = true
                            currentKey = ""
                            currentSize = 0L
                            currentLastModified = ""
                            currentEtag = ""
                            currentStorageClass = "STANDARD"
                        }
                        "CommonPrefixes" -> {
                            inCommonPrefixes = true
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    if (text.isNotBlank()) {
                        when {
                            inContents -> {
                                when (currentTag) {
                                    "Key" -> currentKey = text
                                    "Size" -> currentSize = text.toLongOrNull() ?: 0L
                                    "LastModified" -> currentLastModified = text
                                    "ETag" -> currentEtag = text.replace("\"", "")
                                    "StorageClass" -> currentStorageClass = text
                                }
                            }
                            inCommonPrefixes -> {
                                if (currentTag == "Prefix") {
                                    commonPrefixes.add(text)
                                }
                            }
                            currentTag == "IsTruncated" -> {
                                isTruncated = text.equals("true", ignoreCase = true)
                            }
                            currentTag == "NextContinuationToken" -> {
                                nextContinuationToken = text
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "Contents" -> {
                            inContents = false
                            if (currentKey.isNotBlank() && !currentKey.endsWith("/")) {
                                val ext = currentKey.substringAfterLast('.', "").lowercase()
                                if (ext in audioExtensions) {
                                    val fileName = currentKey.substringAfterLast('/')
                                    val streamUrl = buildPlayableStreamUrl(
                                        bucketName = bucketName,
                                        endpoint = endpoint,
                                        key = currentKey,
                                        usePathStyle = usePathStyle,
                                        useTls = useTls,
                                        region = region,
                                        accessKeyId = accessKeyId,
                                        secretAccessKey = secretAccessKey,
                                        isPublicAccess = isPublicAccess
                                    )

                                    // Estimate rough duration based on file size and format bitrate
                                    val durationSec = estimateAudioDurationSec(currentSize, ext)

                                    tracks.add(
                                        S3AudioTrack(
                                            id = 0, // auto-generated by Room
                                            bucketName = bucketName,
                                            key = currentKey,
                                            title = fileName,
                                            artistOrProject = "S3 远端对象 · $bucketName",
                                            format = ext.uppercase(),
                                            sampleRate = if (ext == "flac" || ext == "wav") "48kHz / 24-bit" else "44.1kHz / 16-bit",
                                            channels = "2.0 Stereo",
                                            sizeBytes = currentSize,
                                            durationMs = durationSec * 1000L,
                                            etag = currentEtag,
                                            storageClass = currentStorageClass,
                                            streamUrl = streamUrl,
                                            isCachedLocally = false,
                                            waveformPointsRaw = generateSimpleWaveform(currentKey),
                                            isFavorite = false,
                                            lastModified = formatIsoTimestamp(currentLastModified)
                                        )
                                    )
                                }
                            }
                        }
                        "CommonPrefixes" -> {
                            inCommonPrefixes = false
                        }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        return S3ListResult(
            tracks = tracks,
            commonPrefixes = commonPrefixes.distinct(),
            isTruncated = isTruncated,
            nextContinuationToken = nextContinuationToken
        )
    }

    /**
     * Generates a playable streaming URL (Presigned if credentials present, or Direct URL if public).
     */
    fun buildPlayableStreamUrl(
        bucketName: String,
        endpoint: String,
        key: String,
        usePathStyle: Boolean,
        useTls: Boolean,
        region: String,
        accessKeyId: String,
        secretAccessKey: String,
        isPublicAccess: Boolean
    ): String {
        val cleanEndpoint = endpoint.trim().removePrefix("https://").removePrefix("http://").trimEnd('/')
        val scheme = if (useTls) "https" else "http"
        val host = if (usePathStyle) cleanEndpoint else (if (cleanEndpoint.startsWith("$bucketName.")) cleanEndpoint else "$bucketName.$cleanEndpoint")
        val path = if (usePathStyle) "/$bucketName/$key" else "/$key"

        if (isPublicAccess || accessKeyId.isBlank() || secretAccessKey.isBlank()) {
            return "$scheme://$host$path"
        }

        // Generate SigV4 Presigned URL valid for 24 hours (86400 seconds)
        return try {
            val dateStamp = getDateStamp()
            val amzDate = getAmzDate()
            val expires = "86400"
            val credentialScope = "$dateStamp/$region/s3/aws4_request"

            val queryParams = mutableListOf(
                "X-Amz-Algorithm" to "AWS4-HMAC-SHA256",
                "X-Amz-Credential" to "$accessKeyId/$credentialScope",
                "X-Amz-Date" to amzDate,
                "X-Amz-Expires" to expires,
                "X-Amz-SignedHeaders" to "host"
            )
            queryParams.sortBy { it.first }

            val canonicalQuery = queryParams.joinToString("&") {
                "${urlEncode(it.first)}=${urlEncode(it.second)}"
            }

            val canonicalHeaders = "host:$host\n"
            val signedHeaders = "host"
            val payloadHash = "UNSIGNED-PAYLOAD"

            val canonicalRequest = "GET\n$path\n$canonicalQuery\n$canonicalHeaders\n$signedHeaders\n$payloadHash"
            val stringToSign = "AWS4-HMAC-SHA256\n$amzDate\n$credentialScope\n${sha256Hex(canonicalRequest)}"

            val signingKey = getSignatureKey(secretAccessKey, dateStamp, region, "s3")
            val signature = hmacSha256Hex(signingKey, stringToSign)

            "$scheme://$host$path?$canonicalQuery&X-Amz-Signature=$signature"
        } catch (e: Exception) {
            "$scheme://$host$path"
        }
    }

    private fun estimateAudioDurationSec(sizeBytes: Long, format: String): Long {
        if (sizeBytes <= 0) return 180L
        return when (format) {
            "flac" -> (sizeBytes / 120_000L).coerceIn(10L, 7200L) // ~960kbps
            "wav" -> (sizeBytes / 176_400L).coerceIn(5L, 7200L)   // 16-bit 44.1k stereo uncompressed
            "mp3" -> (sizeBytes / 40_000L).coerceIn(15L, 7200L)   // ~320kbps
            "m4a", "aac" -> (sizeBytes / 32_000L).coerceIn(15L, 7200L) // ~256kbps
            "ogg", "opus" -> (sizeBytes / 24_000L).coerceIn(15L, 7200L) // ~192kbps
            else -> (sizeBytes / 50_000L).coerceIn(15L, 7200L)
        }
    }

    private fun generateSimpleWaveform(seedKey: String): String {
        val hash = seedKey.hashCode()
        val random = java.util.Random(hash.toLong())
        return (0..48).joinToString(",") {
            String.format(Locale.US, "%.2f", 0.15f + random.nextFloat() * 0.8f)
        }
    }

    private fun formatIsoTimestamp(isoString: String): String {
        return try {
            isoString.replace("T", " ").replace("Z", " UTC").take(19)
        } catch (_: Exception) {
            isoString
        }
    }

    private fun parseS3ErrorMessage(xml: String): String {
        return try {
            val messageStart = xml.indexOf("<Message>")
            val messageEnd = xml.indexOf("</Message>")
            if (messageStart >= 0 && messageEnd > messageStart) {
                xml.substring(messageStart + 9, messageEnd)
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")
    }

    private fun getAmzDate(): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun getDateStamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun sha256Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        val bytes = hmacSha256(key, data)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
        val kSecret = ("AWS4$key").toByteArray(StandardCharsets.UTF_8)
        val kDate = hmacSha256(kSecret, dateStamp)
        val kRegion = hmacSha256(kDate, regionName)
        val kService = hmacSha256(kRegion, serviceName)
        return hmacSha256(kService, "aws4_request")
    }
}
