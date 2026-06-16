package eu.kanade.tachiyomi.extension.pt.littletyrant

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.Source
import okio.buffer
import org.jsoup.nodes.Document

class Decoder {
    fun extractPaths(document: Document, baseUrl: String): List<String> {
        val urlScript = document.selectFirst("script:containsData(var _proxyUrls)")?.data()
            ?: document.selectFirst("script:containsData(var pages)")?.data()
            ?: error("No image URLs")

        val proxyPaths = extractProxyPaths(urlScript, baseUrl)
        if (proxyPaths.isNotEmpty()) {
            return proxyPaths
        }

        val match = PAGES_REGEX.find(urlScript) ?: error("Unable to parse pages")
        return match.groupValues[1]
            .split(",")
            .map { it.trim().trim('"').trim('\'') }
            .filter { it.isNotEmpty() }
            .map { base64 ->
                Base64.decode(base64, Base64.DEFAULT)
                    .toString(Charsets.UTF_8)
                    .trim()
            }
    }

    fun decodeImage(response: Response, key: String): Response {
        if (key.isEmpty()) return response

        val responseBody = response.body
        val source = responseBody.source()
        var offset = 0L
        val decryptedSource = object : Source {
            override fun read(sink: Buffer, byteCount: Long): Long {
                val chunk = Buffer()
                val read = source.read(chunk, byteCount)
                if (read <= 0L) return read

                val bytes = chunk.readByteArray(read)
                val decryptLength = minOf(bytes.size.toLong(), maxOf(0L, ENCRYPTED_PREFIX_SIZE - offset)).toInt()
                for (index in 0 until decryptLength) {
                    val keyIndex = ((offset + index) % key.length).toInt()
                    bytes[index] = (bytes[index].toInt() xor key[keyIndex].code).toByte()
                }
                offset += read

                sink.write(bytes)
                return read
            }

            override fun timeout() = source.timeout()

            override fun close() = source.close()
        }.buffer()

        return response.newBuilder()
            .body(decryptedSource.asResponseBody(IMAGE_MEDIA_TYPE, responseBody.contentLength()))
            .build()
    }

    private fun extractProxyPaths(script: String, baseUrl: String): List<String> {
        val match = PROXY_URLS_REGEX.find(script) ?: return emptyList()
        return URL_REGEX.findAll(match.groupValues[1])
            .map { it.groupValues[1].toAbsoluteUrl(baseUrl) }
            .toList()
    }

    companion object {
        private const val ENCRYPTED_PREFIX_SIZE = 1024L
        private val IMAGE_MEDIA_TYPE = "image/jpeg".toMediaType()
        private val PAGES_REGEX = Regex(
            """var pages\s*=\s*\[(.*?)\]""",
            RegexOption.DOT_MATCHES_ALL,
        )
        private val PROXY_URLS_REGEX = Regex(
            """var\s+_proxyUrls\s*=\s*\[(.*?)]""",
            RegexOption.DOT_MATCHES_ALL,
        )
        private val URL_REGEX = Regex("""['"]([^'"]+image-loader\.php\?path=[^'"]+)['"]""")
    }
}

private fun String.toAbsoluteUrl(baseUrl: String): String = when {
    startsWith("http") -> this
    startsWith("//") -> "https:$this"
    startsWith("/") -> "$baseUrl$this"
    else -> "$baseUrl/$this"
}
