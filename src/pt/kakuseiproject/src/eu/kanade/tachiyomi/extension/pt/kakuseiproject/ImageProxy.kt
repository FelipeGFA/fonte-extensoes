package eu.kanade.tachiyomi.extension.pt.kakuseiproject

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.ForwardingSource
import okio.buffer
import okio.source
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.util.ArrayDeque
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ImageProxy(
    baseUrl: String,
) : Interceptor {

    private val baseHost = baseUrl.toHttpUrl().host
    private val baseOrigin = "$baseUrl/"
    private val context: Application by injectLazy()
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val requestIds = AtomicInteger()
    private val sessions = ConcurrentHashMap<Int, ImageDownloadSession>()
    private val pendingScripts = ArrayDeque<String>()
    private var webView: WebView? = null
    private var webViewReady = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.shouldProxyImage()) {
            return chain.proceed(request)
        }

        return downloadImageWithWebView(request.url.toString())
            ?.toResponse(request, chain.connection()?.protocol() ?: Protocol.HTTP_1_1)
            ?: chain.proceed(request)
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun downloadImageWithWebView(url: String): WebViewImage? {
        val id = requestIds.incrementAndGet()
        val session = ImageDownloadSession(
            file = File.createTempFile("kakusei-image-", ".tmp", context.cacheDir),
            onComplete = { sessions.remove(id) },
        )
        sessions[id] = session

        handler.post {
            enqueueImageFetch(id, url)
        }

        session.await()
        return session.image ?: run {
            session.cancel()
            null
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun enqueueImageFetch(id: Int, url: String) {
        val script = "window.kakuseiFetchImage($id, ${url.toJsStringLiteral()});"

        val view = try {
            webView ?: WebView(context).also { createdView ->
                CookieManager.getInstance().setAcceptCookie(true)

                with(createdView.settings) {
                    javaScriptEnabled = true
                    blockNetworkImage = true
                    loadsImagesAutomatically = false
                }

                createdView.addJavascriptInterface(ImageBridgeInterface(sessions), IMAGE_BRIDGE_INTERFACE)
                createdView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        webViewReady = true

                        while (pendingScripts.isNotEmpty()) {
                            view.evaluateJavascript(pendingScripts.removeFirst(), null)
                        }
                    }
                }
                createdView.loadDataWithBaseURL(baseOrigin, IMAGE_FETCH_HTML, "text/html", "utf-8", null)
                webView = createdView
            }
        } catch (_: Throwable) {
            sessions[id]?.cancel()
            return
        }

        if (webViewReady) {
            view.evaluateJavascript(script, null)
        } else {
            pendingScripts.add(script)
        }
    }

    private fun Request.shouldProxyImage(): Boolean {
        if (url.host != baseHost) return false
        if (method != "GET") return false

        val path = url.encodedPath.lowercase(Locale.ROOT)
        val acceptsImage = header("Accept")?.contains("image/", ignoreCase = true) == true
        return acceptsImage || IMAGE_EXTENSIONS.any(path::endsWith)
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

    private class ImageBridgeInterface(
        private val sessions: ConcurrentHashMap<Int, ImageDownloadSession>,
    ) {
        @JavascriptInterface
        @Suppress("UNUSED")
        fun startImage(id: Int, contentType: String) {
            sessions[id]?.start(contentType)
        }

        @JavascriptInterface
        @Suppress("UNUSED")
        fun passChunk(id: Int, chunk: String) {
            sessions[id]?.passChunk(chunk)
        }

        @JavascriptInterface
        @Suppress("UNUSED")
        fun finishImage(id: Int) {
            sessions[id]?.finish()
        }

        @JavascriptInterface
        @Suppress("UNUSED")
        fun passError(id: Int) {
            sessions[id]?.cancel()
        }
    }

    private class ImageDownloadSession(
        private val file: File,
        private val onComplete: () -> Unit,
    ) {
        private val latch = CountDownLatch(1)
        private val completed = AtomicBoolean(false)
        private var output = file.outputStream()
        private var mediaType = "image/jpeg"

        @Volatile
        var image: WebViewImage? = null
            private set

        fun await() {
            latch.await(IMAGE_PROXY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }

        fun start(contentType: String) {
            mediaType = contentType
                .substringBefore(";")
                .takeIf { it.startsWith("image/") }
                ?: "image/jpeg"
        }

        @Synchronized
        fun passChunk(chunk: String) {
            if (completed.get()) return

            try {
                output.write(Base64.decode(chunk, Base64.DEFAULT))
            } catch (_: Throwable) {
                complete(null)
            }
        }

        @Synchronized
        fun finish() {
            if (completed.get()) return

            runCatching { output.close() }
                .onFailure {
                    complete(null)
                    return
                }

            val length = file.length()
            complete(
                if (length > 0L) {
                    WebViewImage(mediaType, file, length)
                } else {
                    null
                },
            )
        }

        @Synchronized
        fun cancel() {
            complete(null)
        }

        private fun complete(result: WebViewImage?) {
            if (!completed.compareAndSet(false, true)) return

            runCatching { output.close() }
            if (result == null) {
                file.delete()
            }

            image = result
            onComplete()
            latch.countDown()
        }
    }

    private class WebViewImage(
        val mediaType: String,
        val file: File,
        val length: Long,
    ) {
        fun toResponse(request: Request, protocol: Protocol): Response = Response.Builder()
            .request(request)
            .protocol(protocol)
            .code(200)
            .message("OK")
            .header("Content-Type", mediaType)
            .header("Content-Length", length.toString())
            .body(file.asDeletingResponseBody(mediaType.toMediaTypeOrNull(), length))
            .build()
    }

    private companion object {
        const val IMAGE_BRIDGE_INTERFACE = "KakuseiImageBridge"
        const val IMAGE_PROXY_TIMEOUT_SECONDS = 8L
        val IMAGE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".webp", ".gif")

        val IMAGE_FETCH_HTML = """
            <!doctype html>
            <script>
            window.kakuseiFetchImage = function(id, url) {
              function passChunk(value) {
                var chunkSize = 0x10000;
                for (var i = 0; i < value.length; i += chunkSize) {
                  var binary = "";
                  var end = Math.min(i + chunkSize, value.length);
                  for (var j = i; j < end; j++) {
                    binary += String.fromCharCode(value[j]);
                  }
                  window.KakuseiImageBridge.passChunk(id, btoa(binary));
                }
              }

              fetch(url, { credentials: "include" })
                .then(function(response) {
                  if (!response.ok || !response.body || !response.body.getReader) {
                    window.KakuseiImageBridge.passError(id);
                    return;
                  }

                  window.KakuseiImageBridge.startImage(id, response.headers.get("content-type") || "");

                  var reader = response.body.getReader();
                  function read() {
                    reader.read()
                      .then(function(result) {
                        if (result.done) {
                          window.KakuseiImageBridge.finishImage(id);
                          return;
                        }

                        passChunk(result.value);
                        read();
                      })
                      .catch(function() { window.KakuseiImageBridge.passError(id); });
                  }
                  read();
                })
                .catch(function() { window.KakuseiImageBridge.passError(id); });
            };
            </script>
        """.trimIndent()
    }
}

private fun File.asDeletingResponseBody(mediaType: MediaType?, length: Long): ResponseBody {
    val file = this
    val source = object : ForwardingSource(file.source()) {
        override fun close() {
            try {
                super.close()
            } finally {
                file.delete()
            }
        }
    }

    return source.buffer().asResponseBody(mediaType, length)
}
