package eu.kanade.tachiyomi.extension.pt.kuromangas

import keiyoushi.utils.JSON_MEDIA_TYPE
import keiyoushi.utils.parseAs
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class KuroWebViewDecrypt(
    private val baseUrl: String,
    private val tokenProvider: () -> String,
    private val webViewInterceptor: KuroWebViewInterceptor = KuroWebViewInterceptor(),
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (!url.contains("/api/")) {
            return chain.proceed(request)
        }

        val token = tokenProvider()
        if (token.isBlank()) {
            requireKuroLogin()
        }

        val decryptedData = tryWebViewDecrypt(url)
        if (decryptedData == null) {
            requireKuroLogin()
        }

        return Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(decryptedData.toResponseBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun tryWebViewDecrypt(apiUrl: String): String? {
        val pageUrl = getPageUrlForApi(apiUrl)
        val expectPages = apiUrl.contains("/api/chapters/") && apiUrl.contains("/read")
        return webViewInterceptor.getDecryptedData(pageUrl, expectPages)
            ?.let { normalizeCapturedPayload(apiUrl, it) }
    }

    private fun getPageUrlForApi(apiUrl: String) = when {
        apiUrl.contains("/api/mangas") && !apiUrl.contains("/api/mangas/") -> buildCatalogUrl(apiUrl)
        apiUrl.contains("/api/mangas/") -> "$baseUrl/manga/${apiUrl.substringAfter("/api/mangas/").substringBefore("?")}"
        apiUrl.contains("/api/chapters/") && apiUrl.contains("/read") -> {
            val sourceUrl = apiUrl.toHttpUrl()
            val chapterId = apiUrl.substringAfter("/api/chapters/").substringBefore("/read")
            val mangaId = sourceUrl.queryParameter("manga_id") ?: error("manga_id ausente na URL de leitura")
            "$baseUrl/read/$mangaId/$chapterId"
        }
        else -> "$baseUrl/catalog"
    }

    private fun buildCatalogUrl(apiUrl: String): String {
        val sourceUrl = apiUrl.toHttpUrl()
        val targetUrl = "$baseUrl/catalog".toHttpUrl().newBuilder()

        sourceUrl.queryParameter("page")?.takeIf { it != "1" }?.let {
            targetUrl.addQueryParameter("page", it)
        }
        sourceUrl.queryParameter("sort")?.takeIf { it.isNotBlank() }?.let {
            targetUrl.addQueryParameter("sort", it)
        }
        sourceUrl.queryParameter("search")?.takeIf { it.isNotBlank() }?.let {
            targetUrl.addQueryParameter("search", it)
        }

        return targetUrl.build().toString()
    }

    private fun normalizeCapturedPayload(apiUrl: String, data: String): String {
        val root = runCatching { data.parseAs<JsonObject>() }.getOrNull() ?: return data

        val payload = when {
            apiUrl.contains("/api/mangas") && !apiUrl.contains("/api/mangas/") -> root.unwrapDynamicPayload { it.hasMangaListPayload() }
            apiUrl.contains("/api/mangas/") -> root.unwrapDynamicPayload { it.hasMangaDetailsPayload() }
            apiUrl.contains("/api/chapters/") && apiUrl.contains("/read") -> root.unwrapDynamicPayload { it.hasPagesPayload() }
            else -> null
        }

        return payload?.toString() ?: data
    }

    private fun JsonObject.unwrapDynamicPayload(matches: (JsonObject) -> Boolean): JsonObject? {
        if (matches(this)) return this

        return values
            .asSequence()
            .mapNotNull { it as? JsonObject }
            .firstOrNull(matches)
    }

    private fun JsonObject.hasMangaListPayload() = this["data"] is JsonArray && this["pagination"] is JsonObject

    private fun JsonObject.hasMangaDetailsPayload() = this["manga"] is JsonObject && this["chapters"] is JsonArray

    private fun JsonObject.hasPagesPayload() = this["pages"] is JsonArray
}
