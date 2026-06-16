package eu.kanade.tachiyomi.extension.pt.remangas

import android.util.Base64
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.MessageDigest
import java.util.Locale

class NixSignatureInterceptor(
    private val signerClient: OkHttpClient,
    private val apiOrigin: String,
    private val siteOrigin: String,
    private val siteId: String,
) : Interceptor {

    @Volatile
    private var cachedSigner: SignerData? = null

    @Volatile
    private var signerExpiresAt: Long = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.shouldSign()) return chain.proceed(request)

        val signer = getSigner()
        val signedRequest = signer?.let { request.signWith(it) } ?: request
        return chain.proceed(signedRequest)
    }

    private fun Request.shouldSign(): Boolean {
        if (url.host != apiHost) return false

        val path = url.encodedPath
        return path == COMICS_PATH ||
            path.startsWith("$COMICS_PATH/") ||
            path == CHAPTERS_PATH ||
            path.startsWith("$CHAPTERS_PATH/")
    }

    private fun Request.signWith(signer: SignerData): Request {
        val signature = signer.sign(method, url.encodedPath, siteId)

        return newBuilder()
            .header("Accept", "*/*")
            .header("Content-Type", "application/json")
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .header("Sec-Fetch-Dest", "empty")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Site", "cross-site")
            .header("X-Site-ID", siteId)
            .header("X-Web-Slot", signer.slot)
            .header("X-Web-Token", signer.token)
            .header("X-Web-Signature", signature)
            .build()
    }

    @Synchronized
    private fun getSigner(): SignerData? {
        val now = System.currentTimeMillis()
        cachedSigner?.takeIf { now < signerExpiresAt }?.let { return it }

        val request = Request.Builder()
            .url("$apiOrigin/_nix/signer.js?v=$now")
            .header("Accept", "*/*")
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .header("Origin", siteOrigin)
            .header("Referer", "$siteOrigin/")
            .header("Sec-Fetch-Dest", "script")
            .header("Sec-Fetch-Mode", "no-cors")
            .header("Sec-Fetch-Site", "cross-site")
            .build()

        val signer = runCatching {
            signerClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null

                response.body.string().parseSigner()
            }
        }.getOrNull() ?: return cachedSigner

        cachedSigner = signer
        signerExpiresAt = now + SIGNER_TTL_MILLIS
        return signer
    }

    private fun String.parseSigner(): SignerData? {
        val values = SIGNER_ARRAY_REGEX.find(this)
            ?.groupValues
            ?.get(1)
            ?.let { SIGNER_VALUE_REGEX.findAll(it).map { match -> match.groupValues[1] }.toList() }
            ?: return null

        if (values.size < 5) return null

        return SignerData(
            slot = values[0].reversed(),
            secret = values.subList(1, 4).joinToString("") { it.reversed() },
            token = values.subList(4, values.size).joinToString("") { it.reversed() },
        )
    }

    private val apiHost = apiOrigin.toHttpUrl().host

    private class SignerData(
        val slot: String,
        val secret: String,
        val token: String,
    ) {
        fun sign(method: String, path: String, siteId: String): String {
            val payload = listOf(
                method.uppercase(Locale.ROOT),
                path.substringBefore("?"),
                siteId,
                slot,
                token,
                secret,
            ).joinToString("|")

            val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(
                digest,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
            )
        }
    }

    private companion object {
        const val COMICS_PATH = "/api/v1/comics"
        const val CHAPTERS_PATH = "/api/v1/chapters"
        const val SIGNER_TTL_MILLIS = 15 * 60 * 1000L
        val SIGNER_ARRAY_REGEX = Regex("""const z=\[(.*?)]""", RegexOption.DOT_MATCHES_ALL)
        val SIGNER_VALUE_REGEX = Regex(""""([^"]*)"""")
    }
}
