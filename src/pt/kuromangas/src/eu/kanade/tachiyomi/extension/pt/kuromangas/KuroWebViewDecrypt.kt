package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.IOException

class KuroWebViewDecrypt(
    private val baseUrl: String,
    private val credentialsProvider: () -> Pair<String, String>,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (!url.contains("/api/")) return chain.proceed(request)

        val credentials = credentialsProvider()
        if (credentials.first.isBlank() || credentials.second.isBlank()) {
            showToast("Faça o login nas configurações da extensão")
            throw IOException("Faça o login nas configurações da extensão")
        }

        val decryptedData = tryWebViewDecrypt(url)
        if (decryptedData == null) {
            showToast("Falha no login - verifique suas credenciais")
            throw IOException("Email ou senha incorretos")
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
        val endpoint = apiUrl.substringAfter("/api/").substringBefore("?")

        val loginCredentials = if (apiUrl.contains("/api/auth/login")) {
            credentialsProvider().let { KuroWebViewInterceptor.LoginCredentials(it.first, it.second) }
        } else {
            null
        }

        return KuroWebViewInterceptor().getDecryptedData(pageUrl, endpoint, loginCredentials)?.data
    }

    private fun getPageUrlForApi(apiUrl: String) = when {
        apiUrl.contains("/api/mangas") && !apiUrl.contains("/api/mangas/") -> buildCatalogoUrl(apiUrl)
        apiUrl.contains("/api/mangas/") -> "$baseUrl/manga/${apiUrl.substringAfter("/api/mangas/").substringBefore("?")}"
        apiUrl.contains("/api/chapters/") && apiUrl.contains("/read") -> {
            val chapterId = apiUrl.substringAfter("/api/chapters/").substringBefore("/read")
            val mangaId = apiUrl.substringAfter("manga_id=", "").substringBefore("&").ifEmpty { "0" }
            "$baseUrl/reader/$mangaId/$chapterId"
        }
        apiUrl.contains("/api/auth/login") -> "$baseUrl/login"
        else -> "$baseUrl/catalogo"
    }

    private fun buildCatalogoUrl(apiUrl: String): String {
        val params = mutableListOf<String>()
        apiUrl.substringAfter("page=", "").substringBefore("&").takeIf { it.isNotEmpty() && it != "1" }?.let { params.add("page=$it") }
        apiUrl.substringAfter("sort=", "").substringBefore("&").takeIf { it.isNotEmpty() }?.let { params.add("sort=$it") }
        apiUrl.substringAfter("search=", "").substringBefore("&").takeIf { it.isNotEmpty() }?.let { params.add("search=$it") }
        return if (params.isNotEmpty()) "$baseUrl/catalogo?${params.joinToString("&")}" else "$baseUrl/catalogo"
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(Injekt.get<Application>(), message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
