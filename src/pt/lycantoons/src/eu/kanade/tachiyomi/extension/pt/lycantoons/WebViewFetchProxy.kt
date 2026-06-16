package eu.kanade.tachiyomi.extension.pt.lycantoons

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import uy.kohesive.injekt.injectLazy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class WebViewFetchProxy(
    baseUrl: String,
) : Interceptor {

    private val app: Application by injectLazy()
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val homeUrl = "$baseUrl/"
    private val host = baseUrl.toHttpUrl().host

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.shouldProxy()) return chain.proceed(request)

        return fetch(request)?.toResponse(request) ?: chain.proceed(request)
    }

    private fun Request.shouldProxy(): Boolean {
        if (url.host != host) return false
        return method == "GET" || method == "POST"
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun fetch(request: Request): WebViewFetchResult? {
        val latch = CountDownLatch(1)
        val completed = AtomicBoolean(false)
        val result = AtomicReference<WebViewFetchResult?>()
        val webViewRef = AtomicReference<WebView?>()
        val bridge = FetchBridge(latch, completed, result)

        handler.post {
            try {
                CookieManager.getInstance().setAcceptCookie(true)

                val webView = WebView(app).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.blockNetworkImage = true
                    settings.loadsImagesAutomatically = false
                    addJavascriptInterface(bridge, BRIDGE_NAME)
                }

                webViewRef.set(webView)
                webView.loadDataWithBaseURL(homeUrl, request.toFetchHtml(), "text/html", "UTF-8", null)
            } catch (_: Throwable) {
                if (completed.compareAndSet(false, true)) {
                    latch.countDown()
                }
            }
        }

        val finished = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        handler.post {
            webViewRef.getAndSet(null)?.destroy()
        }

        return if (finished) result.get() else null
    }

    private fun Request.toFetchHtml(): String {
        val requestBody = bodyString()
        val headers = buildMap {
            put("Accept", header("Accept") ?: "*/*")

            val contentType = body?.contentType()?.toString()
            if (contentType != null) put("Content-Type", contentType)
        }

        return """
            <!doctype html>
            <html>
            <body>
            <script>
            (() => {
                const options = {
                    method: ${method.toJsStringLiteral()},
                    credentials: "include",
                    cache: "no-store",
                    headers: ${headers.toJsObjectLiteral()},
                };
                const body = ${requestBody?.toJsStringLiteral() ?: "null"};
                if (body !== null) options.body = body;

                fetch(${url.toString().toJsStringLiteral()}, options)
                    .then(async response => {
                        const text = await response.text();
                        window.$BRIDGE_NAME.complete(
                            response.status,
                            response.statusText || "",
                            response.headers.get("content-type") || "",
                            text
                        );
                    })
                    .catch(error => {
                        window.$BRIDGE_NAME.fail(String(error && error.message ? error.message : error));
                    });
            })();
            </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun Request.bodyString(): String? {
        val requestBody = body ?: return null
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }

    private fun Map<String, String>.toJsObjectLiteral(): String = entries.joinToString(
        prefix = "{",
        postfix = "}",
    ) { (key, value) ->
        "${key.toJsStringLiteral()}: ${value.toJsStringLiteral()}"
    }

    private fun String.toJsStringLiteral(): String = buildString {
        append('"')
        this@toJsStringLiteral.forEach {
            when (it) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(it)
            }
        }
        append('"')
    }

    private class FetchBridge(
        private val latch: CountDownLatch,
        private val completed: AtomicBoolean,
        private val result: AtomicReference<WebViewFetchResult?>,
    ) {
        @JavascriptInterface
        fun complete(status: Int, statusText: String, contentType: String, body: String) {
            if (!completed.compareAndSet(false, true)) return

            result.set(WebViewFetchResult(status, statusText, contentType, body))
            latch.countDown()
        }

        @JavascriptInterface
        @Suppress("UNUSED_PARAMETER")
        fun fail(message: String) {
            if (completed.compareAndSet(false, true)) {
                latch.countDown()
            }
        }
    }

    private class WebViewFetchResult(
        private val status: Int,
        private val statusText: String,
        private val contentType: String,
        private val body: String,
    ) {
        fun toResponse(request: Request): Response {
            val mediaType = contentType.takeIf(String::isNotBlank)?.toMediaTypeOrNull()

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(status.takeIf { it >= 100 } ?: 200)
                .message(statusText.ifBlank { "OK" })
                .body(body.toResponseBody(mediaType))
                .also {
                    if (contentType.isNotBlank()) it.header("Content-Type", contentType)
                }
                .build()
        }
    }

    companion object {
        private const val BRIDGE_NAME = "LycanFetchBridge"
        private const val TIMEOUT_SECONDS = 15L
    }
}
