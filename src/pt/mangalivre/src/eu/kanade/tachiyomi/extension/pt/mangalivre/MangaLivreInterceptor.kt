package eu.kanade.tachiyomi.extension.pt.mangalivre

import android.util.Base64
import eu.kanade.tachiyomi.network.GET
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.security.MessageDigest

class MangaLivreInterceptor(
    private val baseUrl: String,
    private val client: OkHttpClient,
    private val headers: okhttp3.Headers,
) : Interceptor {

    private var toonClient: String? = null
    private var toonSecret: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val path = request.url.encodedPath

        if (!url.contains("/api/")) {
            return chain.proceed(request)
        }

        if (toonClient == null || toonSecret == null) {
            fetchTokens()
        }

        val builder = request.newBuilder()

        toonClient?.let {
            builder.header("x-toonlivre-client", it)
        }

        if (path.contains("/chapters/")) {
            val time = System.currentTimeMillis().toString()
            val secret = toonSecret ?: ""
            val ce = "$path|$time|$secret"
            val hash = MessageDigest.getInstance("SHA-256")
                .digest(ce.toByteArray())
                .joinToString("") { "%02x".format(it) }

            builder.header("x-toon-time", time)
            builder.header("x-toon-sig", hash)
        }

        return chain.proceed(builder.build())
    }

    @Synchronized
    private fun fetchTokens() {
        if (toonClient != null && toonSecret != null) return

        try {
            val response = client.newCall(GET(baseUrl, headers)).execute()
            val body = response.body.string()

            val scriptUrlRegex = """<script[^>]*src="([^"]*index-[^"]*\.js)"""".toRegex()
            val scriptPath = scriptUrlRegex.find(body)?.groupValues?.get(1) ?: return

            val scriptUrl = if (scriptPath.startsWith("http")) scriptPath else "$baseUrl$scriptPath"

            val scriptResponse = client.newCall(GET(scriptUrl, headers)).execute()
            val scriptBody = scriptResponse.body.string()

            val clientRegex = """set\(['"]x-toonlivre-client['"],\s*['"]([^'"]+)['"]\)""".toRegex()
            toonClient = clientRegex.find(scriptBody)?.groupValues?.get(1)

            val secretRegex = """atob\(['"]([^'"]+)['"]\)\.split\(['"]['"]\)\.reverse\(\)\.join\(['"]['"]\)""".toRegex()
            val base64Secret = secretRegex.find(scriptBody)?.groupValues?.get(1)

            if (base64Secret != null) {
                toonSecret = String(Base64.decode(base64Secret, Base64.DEFAULT)).reversed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
