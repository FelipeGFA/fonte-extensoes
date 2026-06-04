package eu.kanade.tachiyomi.extension.pt.kakuseiproject

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import uy.kohesive.injekt.injectLazy
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HtmlProxy(
    baseUrl: String,
) : Interceptor {

    private val baseHost = baseUrl.toHttpUrl().host
    private val context: Application by injectLazy()
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val proxyResult = when {
            request.shouldProxyPageHtml() -> loadPageHtml(request.url.toString())
            request.shouldProxyChaptersAjax() -> loadChaptersAjax(request.url.toString())
            else -> null
        }

        proxyResult?.let { html ->
            return html.toResponse(request, chain.connection()?.protocol() ?: Protocol.HTTP_1_1)
        }

        return chain.proceed(request)
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun loadPageHtml(url: String): HtmlProxyResult? {
        val latch = CountDownLatch(1)
        val jsInterface = HtmlBridgeInterface(latch)
        var webView: WebView? = null

        handler.post {
            try {
                CookieManager.getInstance().setAcceptCookie(true)

                val view = WebView(context)
                webView = view

                with(view.settings) {
                    javaScriptEnabled = true
                    blockNetworkImage = true
                    loadsImagesAutomatically = false
                }

                view.addJavascriptInterface(jsInterface, HTML_BRIDGE_INTERFACE)

                view.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)
                        view.evaluateJavascript(CAPTURE_HTML_SCRIPT, null)
                    }
                }

                view.loadUrl(url)
            } catch (_: Throwable) {
                latch.countDown()
            }
        }

        return awaitHtml(latch, jsInterface, webView)
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun loadChaptersAjax(url: String): HtmlProxyResult? {
        val latch = CountDownLatch(1)
        val jsInterface = HtmlBridgeInterface(latch)
        var webView: WebView? = null

        handler.post {
            try {
                CookieManager.getInstance().setAcceptCookie(true)

                val view = WebView(context)
                webView = view

                view.settings.javaScriptEnabled = true

                view.addJavascriptInterface(jsInterface, HTML_BRIDGE_INTERFACE)

                view.loadDataWithBaseURL(
                    "https://$baseHost/",
                    CHAPTERS_FETCH_HTML.replace(CHAPTERS_URL_PLACEHOLDER, url.toJsStringLiteral()),
                    "text/html",
                    "utf-8",
                    null,
                )
            } catch (_: Throwable) {
                latch.countDown()
            }
        }

        return awaitHtml(latch, jsInterface, webView)
    }

    private fun awaitHtml(
        latch: CountDownLatch,
        jsInterface: HtmlBridgeInterface,
        webView: WebView?,
    ): HtmlProxyResult? {
        latch.await(HTML_PROXY_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        handler.post {
            webView?.stopLoading()
            webView?.destroy()
        }

        return jsInterface.html
    }

    private fun Request.shouldProxyPageHtml(): Boolean {
        if (method != "GET") return false
        if (url.host != baseHost) return false
        if (isImageRequest()) return false
        if (header("Accept")?.contains("image/", ignoreCase = true) == true) return false

        return url.pathSegments.firstOrNull() == MANGA_PATH_SEGMENT
    }

    private fun Request.shouldProxyChaptersAjax(): Boolean {
        if (method != "POST") return false
        if (url.host != baseHost) return false

        val segments = url.pathSegments
        return segments.firstOrNull() == MANGA_PATH_SEGMENT &&
            segments.takeLast(2) == listOf("ajax", "chapters")
    }

    private fun Request.isImageRequest(): Boolean {
        val path = url.encodedPath.lowercase(Locale.ROOT)
        return IMAGE_EXTENSIONS.any(path::endsWith)
    }

    private fun String.toJsStringLiteral(): String = buildString {
        append('"')
        this@toJsStringLiteral.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(char)
            }
        }
        append('"')
    }

    private class HtmlBridgeInterface(
        private val latch: CountDownLatch,
    ) {
        @Volatile
        var html: HtmlProxyResult? = null
            private set

        @Volatile
        var error = false
            private set

        @JavascriptInterface
        @Suppress("UNUSED")
        fun passHtml(contentType: String, body: String) {
            if (html != null || error) return

            html = HtmlProxyResult(
                contentType = contentType.substringBefore(";").ifEmpty { "text/html" },
                body = body,
            )
            latch.countDown()
        }

        @JavascriptInterface
        @Suppress("UNUSED")
        fun passError() {
            if (html != null || error) return

            error = true
            latch.countDown()
        }
    }

    private class HtmlProxyResult(
        val contentType: String,
        val body: String,
    ) {
        fun toResponse(request: Request, protocol: Protocol): Response = Response.Builder()
            .request(request)
            .protocol(protocol)
            .code(200)
            .message("OK")
            .headers(Headers.headersOf("Content-Type", contentType))
            .body(body.toResponseBody(contentType.toMediaTypeOrNull()))
            .build()
    }

    private companion object {
        const val HTML_BRIDGE_INTERFACE = "KakuseiHtmlProxy"
        const val HTML_PROXY_TIMEOUT_SECONDS = 8L
        const val MANGA_PATH_SEGMENT = "manga"
        const val CHAPTERS_URL_PLACEHOLDER = "__CHAPTERS_URL__"
        val IMAGE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".webp", ".gif")

        val CAPTURE_HTML_SCRIPT = """
            (function() {
              window.KakuseiHtmlProxy.passHtml(
                document.contentType || "text/html",
                document.documentElement ? document.documentElement.outerHTML : ""
              );
            })();
        """.trimIndent()

        val CHAPTERS_FETCH_HTML = """
            <!doctype html>
            <script>
            fetch(__CHAPTERS_URL__, {
              method: "POST",
              credentials: "include",
              headers: {
                "Accept": "text/html, */*; q=0.01",
                "X-Requested-With": "XMLHttpRequest"
              }
            }).then(function(response) {
              return response.text().then(function(text) {
                window.KakuseiHtmlProxy.passHtml(
                  response.headers.get("content-type") || "text/html",
                  text
                );
              });
            }).catch(function() { window.KakuseiHtmlProxy.passError(); });
            </script>
        """.trimIndent()
    }
}
