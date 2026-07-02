package eu.kanade.tachiyomi.extension.pt.lycantoons

import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import keiyoushi.utils.applicationContext
import keiyoushi.utils.toJsonString
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebViewInterceptor(private val baseUrl: String, private val userAgent: String?) : Interceptor {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var latch: CountDownLatch? = null
    private var fetchResult: FetchResult? = null

    private val bridgeName = "LycanBridge"
    private var cachedWebView: WebView? = null
    private var isBaseUrlLoaded = false

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val isImage = url.contains("cdn.lycantoons.com")

        if (!url.contains("/api/") && !url.contains("/series/") && !isImage) {
            return chain.proceed(request)
        }

        latch = CountDownLatch(1)
        fetchResult = null

        val isHtml = url.contains("/series/")

        mainHandler.post {
            try {
                val webView = getWebView()

                val bodyString = request.body?.let { requestBody ->
                    val buffer = okio.Buffer()
                    requestBody.writeTo(buffer)
                    buffer.readUtf8()
                }

                if (isHtml) {
                    var htmlExtracted = false
                    webView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean {
                            if (!request.url.toString().startsWith(baseUrl)) return true
                            return super.shouldOverrideUrlLoading(view, request)
                        }

                        override fun onPageFinished(view: WebView, pageUrl: String?) {
                            if (htmlExtracted) return

                            view.evaluateJavascript(
                                """
                                (function() {
                                    if (document.title === 'Just a moment...' || document.getElementById('challenge-running') != null) {
                                        return 'challenge';
                                    }
                                    window.$bridgeName.passResult(document.documentElement.outerHTML, 'text/html');
                                    return 'ready';
                                })();
                                """.trimIndent(),
                            ) { result ->
                                if (result == "\"ready\"" && !htmlExtracted) {
                                    htmlExtracted = true
                                    isBaseUrlLoaded = true
                                }
                            }
                        }
                    }
                    webView.loadUrl(url)
                } else {
                    if (isBaseUrlLoaded && webView.url?.startsWith(baseUrl) == true) {
                        executeFetch(webView, url, request.method, request.headers, isImage, bodyString)
                    } else {
                        isBaseUrlLoaded = false
                        var fetchExecuted = false
                        webView.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean {
                                if (!request.url.toString().startsWith(baseUrl)) return true
                                return super.shouldOverrideUrlLoading(view, request)
                            }

                            override fun onPageFinished(view: WebView, pageUrl: String?) {
                                if (fetchExecuted) return

                                view.evaluateJavascript(
                                    """
                                    (function() {
                                        if (document.title === 'Just a moment...' || document.getElementById('challenge-running') != null) {
                                            return 'challenge';
                                        }
                                        return 'ready';
                                    })();
                                    """.trimIndent(),
                                ) { result ->
                                    if (result == "\"ready\"" && !fetchExecuted) {
                                        fetchExecuted = true
                                        isBaseUrlLoaded = true
                                        executeFetch(view, url, request.method, request.headers, isImage, bodyString)
                                    }
                                }
                            }
                        }
                        webView.loadUrl("$baseUrl/")
                    }
                }
            } catch (e: Exception) {
                fetchResult = FetchResult(false, "Exception: ${e.message}")
                latch?.countDown()
            }
        }

        latch?.await(30, TimeUnit.SECONDS)

        val finalResult = fetchResult ?: FetchResult(false, "Timeout waiting for WebView fetch")

        if (!finalResult.success || finalResult.result.contains("HTTP 403")) {
            isBaseUrlLoaded = false
            throw IOException("[WebView] API Fetch failed: ${finalResult.result}")
        }

        val contentType = finalResult.contentType ?: if (isImage) "image/jpeg" else "application/json"

        val bodyBytes = if (isImage) {
            Base64.decode(finalResult.result, Base64.DEFAULT)
        } else {
            finalResult.result.toByteArray(Charsets.UTF_8)
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("Content-Type", contentType)
            .body(bodyBytes.toResponseBody(contentType.toMediaTypeOrNull()))
            .build()
    }

    private fun executeFetch(webView: WebView, url: String, method: String, headers: Headers, isImage: Boolean, bodyString: String? = null) {
        if (isImage) {
            val jsScript = """
                (function() {
                    fetch('$url', {
                        method: 'GET',
                        credentials: 'omit'
                    })
                    .then(res => {
                        if (!res.ok) throw new Error('HTTP ' + res.status);
                        return res.blob().then(blob => ({blob, contentType: res.headers.get('content-type')}));
                    })
                    .then(({blob, contentType}) => {
                        const reader = new FileReader();
                        reader.onload = () => {
                            const b64 = reader.result.split(',')[1];
                            window.$bridgeName.passResult(b64, contentType);
                        };
                        reader.onerror = () => window.$bridgeName.passError('Reader error');
                        reader.readAsDataURL(blob);
                    })
                    .catch(err => {
                        const img = document.createElement('img');
                        img.crossOrigin = 'anonymous';
                        img.onload = () => {
                            try {
                                const canvas = document.createElement('canvas');
                                canvas.width = img.naturalWidth;
                                canvas.height = img.naturalHeight;
                                canvas.getContext('2d').drawImage(img, 0, 0);
                                window.$bridgeName.passResult(canvas.toDataURL('image/jpeg', 0.8).split(',')[1], 'image/jpeg');
                            } catch (e) {
                                window.$bridgeName.passError('Canvas error: ' + e.message);
                            }
                        };
                        img.onerror = () => window.$bridgeName.passError('Image load error: ' + err.message);
                        img.src = '$url';
                    });
                })();
            """.trimIndent()
            webView.evaluateJavascript(jsScript, null)
            return
        }

        val jsHeaders = buildMap {
            headers.names().forEach { name ->
                if (name.lowercase() != "user-agent" && name.lowercase() != "referer") {
                    put(name, headers[name])
                }
            }
        }.toJsonString()

        val jsBodyString = if (bodyString != null) {
            "'${bodyString.replace("'", "\\'")}'"
        } else {
            "null"
        }

        val jsScript = """
            (function() {
                let contentType;
                let options = {
                    method: '$method',
                    credentials: 'include',
                    headers: $jsHeaders
                };
                if ($jsBodyString !== null) {
                    options.body = $jsBodyString;
                }

                fetch('$url', options)
                .then(res => {
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    contentType = res.headers.get('content-type');
                    return res.text();
                })
                .then(text => window.$bridgeName.passResult(text, contentType))
                .catch(err => window.$bridgeName.passError(err.message));
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsScript, null)
    }

    private fun getWebView(): WebView {
        if (cachedWebView == null) {
            cachedWebView = WebView(applicationContext).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    userAgentString = userAgent
                }

                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun passResult(data: String, contentType: String?) {
                            fetchResult = FetchResult(true, data, contentType)
                            latch?.countDown()
                        }

                        @JavascriptInterface
                        fun passError(error: String) {
                            fetchResult = FetchResult(false, error)
                            latch?.countDown()
                        }
                    },
                    bridgeName,
                )
            }
        }
        return cachedWebView!!
    }
}

class FetchResult(
    val success: Boolean,
    val result: String,
    val contentType: String? = null,
)
