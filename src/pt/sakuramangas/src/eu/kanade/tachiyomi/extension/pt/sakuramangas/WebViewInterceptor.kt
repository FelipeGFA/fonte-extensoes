package eu.kanade.tachiyomi.extension.pt.sakuramangas

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONTokener
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WebViewInterceptor(
    private val userAgent: String,
    private val httpClient: OkHttpClient,
    private val baseUrl: String,
) {
    private companion object {
        const val TAG = "SakuraWebViewFlow"
        const val TIMEOUT_SECONDS = 45L
        const val ACTIVE_WAIT_MS = 10000L
        const val ACTIVE_WAIT_STEP_MS = 250L
        const val SNAPSHOT_SCRIPT = """
            (function() {
                try {
                    return [
                        'href=' + window.location.href,
                        'title=' + document.title,
                        'readyState=' + document.readyState,
                        'referrer=' + document.referrer,
                        'historyLength=' + history.length,
                        'navigator.userAgent=' + navigator.userAgent,
                        'navigator.platform=' + navigator.platform,
                        'navigator.vendor=' + navigator.vendor,
                        'navigator.webdriver=' + navigator.webdriver,
                        'navigator.language=' + navigator.language,
                        'navigator.languages=' + JSON.stringify(navigator.languages || []),
                        'navigator.cookieEnabled=' + navigator.cookieEnabled
                    ].join('\n');
                } catch (error) {
                    return 'snapshot_error=' + String(error);
                }
            })();
        """
    }

    data class PageLoadResult(
        val initialUrl: String,
        val finalUrl: String?,
        val title: String?,
        val readyState: String?,
        val blocked: Boolean,
        val completed: Boolean,
    )

    private class SessionState(initialUrl: String) {
        val initialUrl: String = initialUrl

        @Volatile var finalUrl: String? = null

        @Volatile var title: String? = null

        @Volatile var readyState: String? = null

        @Volatile var blocked = false
    }

    private val sessionCounter = AtomicInteger(0)

    @Volatile
    private var isWebViewActive = false

    fun inspectPage(pageUrl: String, reason: String): PageLoadResult? {
        if (isWebViewActive) {
            val waitStart = System.currentTimeMillis()
            Log.w(TAG, "Waiting for active WebView session to finish reason=$reason url=$pageUrl")
            while (isWebViewActive && (System.currentTimeMillis() - waitStart) < ACTIVE_WAIT_MS) {
                Thread.sleep(ACTIVE_WAIT_STEP_MS)
            }
            if (isWebViewActive) {
                Log.w(TAG, "Timed out waiting for active WebView session reason=$reason url=$pageUrl")
                return null
            }
        }

        isWebViewActive = true
        val sessionId = sessionCounter.incrementAndGet()
        log(sessionId, reason, "Starting WebView inspection url=$pageUrl")
        logHttpClientPreflight(sessionId, reason, pageUrl)

        return try {
            doInspectPage(sessionId, reason, pageUrl)
        } finally {
            isWebViewActive = false
            log(sessionId, reason, "WebView inspection finished url=$pageUrl")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun doInspectPage(sessionId: Int, reason: String, pageUrl: String): PageLoadResult? {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val released = AtomicBoolean(false)
        val state = SessionState(pageUrl)
        var webView: WebView? = null

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }
                setupWebView(wv, sessionId, reason)
                syncCookiesToWebView(sessionId, reason)

                wv.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val requestUrl = request?.url?.toString().orEmpty()
                        log(
                            sessionId,
                            reason,
                            "shouldOverrideUrlLoading url=$requestUrl method=${request?.method.orEmpty()} mainFrame=${request?.isForMainFrame == true}",
                        )
                        return false
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        state.finalUrl = url
                        state.blocked = state.blocked || url.orEmpty().contains("block.php", ignoreCase = true)
                        log(sessionId, reason, "onPageStarted url=$url")
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {
                        super.onLoadResource(view, url)
                        log(sessionId, reason, "onLoadResource url=$url")
                    }

                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        super.onPageCommitVisible(view, url)
                        state.finalUrl = url
                        state.blocked = state.blocked || url.orEmpty().contains("block.php", ignoreCase = true)
                        log(sessionId, reason, "onPageCommitVisible url=$url")
                    }

                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        val requestUrl = request?.url?.toString().orEmpty()
                        val mainFrame = request?.isForMainFrame == true
                        val method = request?.method.orEmpty()
                        log(sessionId, reason, "shouldInterceptRequest mainFrame=$mainFrame method=$method url=$requestUrl")
                        if (mainFrame) {
                            val headers = request?.requestHeaders
                                ?.entries
                                ?.sortedBy { it.key.lowercase() }
                                ?.joinToString(", ") { "${it.key}=${it.value}" }
                                .orEmpty()
                            log(sessionId, reason, "mainFrameRequestHeaders $headers")
                        }
                        if (requestUrl.contains("block.php", ignoreCase = true)) {
                            state.blocked = true
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        log(
                            sessionId,
                            reason,
                            "onReceivedError code=${error?.errorCode} description=${error?.description} url=${request?.url}",
                        )
                    }

                    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        log(
                            sessionId,
                            reason,
                            "onReceivedHttpError status=${errorResponse?.statusCode} reasonPhrase=${errorResponse?.reasonPhrase} url=${request?.url}",
                        )
                    }

                    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        super.onReceivedSslError(view, handler, error)
                        log(sessionId, reason, "onReceivedSslError primaryError=${error?.primaryError} url=${error?.url}")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        state.finalUrl = url
                        state.blocked = state.blocked || url.orEmpty().contains("block.php", ignoreCase = true)
                        log(sessionId, reason, "onPageFinished url=$url title=${view?.title}")
                        logCookies(sessionId, reason, url)
                        logJavascriptSnapshot(sessionId, reason, view, state)
                        handler.postDelayed(
                            {
                                releaseLatch(sessionId, reason, latch, released, "page-finished")
                            },
                            1500,
                        )
                    }
                }

                wv.webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        log(sessionId, reason, "onProgressChanged progress=$newProgress url=${view?.url}")
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        state.title = title
                        log(sessionId, reason, "onReceivedTitle title=$title")
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        val message = consoleMessage?.message().orEmpty()
                        log(
                            sessionId,
                            reason,
                            "onConsoleMessage level=${consoleMessage?.messageLevel()} source=${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()} message=$message",
                        )
                        return super.onConsoleMessage(consoleMessage)
                    }
                }

                val extraHeaders = buildLoadHeaders()
                wv.loadUrl(pageUrl, extraHeaders)
                log(sessionId, reason, "loadUrl invoked url=$pageUrl extraHeaders=$extraHeaders")
            }.onFailure { error ->
                log(sessionId, reason, "Failed to initialize WebView: ${error.message}", error)
                releaseLatch(sessionId, reason, latch, released, "initialization-failure")
            }
        }

        val completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (!completed) {
            log(sessionId, reason, "Timed out waiting for WebView inspection")
        }

        handler.post {
            logJavascriptSnapshot(sessionId, reason, webView, state)
            webView?.run {
                stopLoading()
                destroy()
            }
            log(sessionId, reason, "WebView destroyed")
        }

        return PageLoadResult(
            initialUrl = state.initialUrl,
            finalUrl = state.finalUrl,
            title = state.title,
            readyState = state.readyState,
            blocked = state.blocked,
            completed = completed,
        ).also { result ->
            log(
                sessionId,
                reason,
                "Result initialUrl=${result.initialUrl} finalUrl=${result.finalUrl} title=${result.title} readyState=${result.readyState} blocked=${result.blocked} completed=${result.completed}",
            )
        }
    }

    private fun releaseLatch(
        sessionId: Int,
        reason: String,
        latch: CountDownLatch,
        released: AtomicBoolean,
        source: String,
    ) {
        if (released.compareAndSet(false, true)) {
            log(sessionId, reason, "Releasing latch source=$source")
            latch.countDown()
        }
    }

    private fun logJavascriptSnapshot(sessionId: Int, reason: String, webView: WebView?, state: SessionState) {
        webView?.evaluateJavascript(
            SNAPSHOT_SCRIPT,
            ValueCallback { rawValue ->
                val decoded = decodeJavascriptValue(rawValue)
                state.readyState = decoded
                    ?.lineSequence()
                    ?.firstOrNull { it.startsWith("readyState=") }
                    ?.substringAfter("readyState=")
                decoded
                    ?.lineSequence()
                    ?.firstOrNull { it.startsWith("href=") }
                    ?.substringAfter("href=")
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { state.finalUrl = it }
                decoded
                    ?.lineSequence()
                    ?.firstOrNull { it.startsWith("title=") }
                    ?.substringAfter("title=")
                    ?.let { state.title = it }
                log(sessionId, reason, "javascriptSnapshot ${decoded ?: rawValue}")
            },
        )
    }

    private fun decodeJavascriptValue(rawValue: String?): String? {
        if (rawValue.isNullOrBlank() || rawValue == "null") return null
        return runCatching {
            when (val value = JSONTokener(rawValue).nextValue()) {
                is String -> value
                else -> rawValue
            }
        }.getOrElse { rawValue }
    }

    private fun syncCookiesToWebView(sessionId: Int, reason: String) {
        runCatching {
            val cookies = httpClient.cookieJar.loadForRequest(baseUrl.toHttpUrl())
            val deduplicatedCookies = cookies.distinctBy { "${it.name}|${it.domain}|${it.path}|${it.value}" }
            val duplicateNames = cookies
                .groupBy { it.name }
                .filterValues { it.size > 1 }
                .keys
                .sorted()
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            val existingCookies = cookieManager.getCookie(baseUrl).orEmpty()
            val existingCookieNames = existingCookies
                .split(";")
                .mapNotNull { cookie ->
                    val pair = cookie.trim().split("=", limit = 2)
                    pair.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                }
                .toSet()
            log(
                sessionId,
                reason,
                "Syncing ${deduplicatedCookies.size} cookies to WebView rawCount=${cookies.size} duplicateNames=${duplicateNames.joinToString(",")} names=${deduplicatedCookies.joinToString(",") { it.name }} existingNames=${existingCookieNames.joinToString(",")}",
            )
            deduplicatedCookies.forEach { cookie ->
                if (cookie.name !in existingCookieNames) {
                    cookieManager.setCookie(baseUrl, "${cookie.name}=${cookie.value}; Domain=${cookie.domain}; Path=${cookie.path}")
                }
            }
            cookieManager.flush()
        }.onFailure { error ->
            log(sessionId, reason, "Failed to sync cookies: ${error.message}", error)
        }
    }

    private fun logCookies(sessionId: Int, reason: String, url: String?) {
        if (url.isNullOrBlank()) return
        runCatching {
            val cookies = CookieManager.getInstance().getCookie(url).orEmpty()
            log(sessionId, reason, "cookieSnapshot url=$url cookies=$cookies")
        }.onFailure { error ->
            log(sessionId, reason, "Failed to read WebView cookies: ${error.message}", error)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView, sessionId: Int, reason: String) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        val nativeUserAgent = webView.settings.userAgentString
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowContentAccess = true
            allowFileAccess = true
            loadsImagesAutomatically = true
            blockNetworkImage = false
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(false)
        }

        log(
            sessionId,
            reason,
            "WebView configured requestedUserAgent=$userAgent nativeUserAgent=$nativeUserAgent activeUserAgent=${webView.settings.userAgentString} javaScriptEnabled=${webView.settings.javaScriptEnabled} domStorageEnabled=${webView.settings.domStorageEnabled}",
        )
    }

    private fun buildLoadHeaders() = mapOf(
        "Accept-Language" to "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7",
        "Cache-Control" to "no-cache",
        "Pragma" to "no-cache",
        "DNT" to "1",
    )

    private fun buildPreflightRequest(url: String) = Request.Builder()
        .url(url)
        .header("User-Agent", userAgent)
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
        .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
        .header("Cache-Control", "no-cache")
        .header("Pragma", "no-cache")
        .header("DNT", "1")
        .header("Upgrade-Insecure-Requests", "1")
        .get()
        .build()

    private fun logHttpClientPreflight(sessionId: Int, reason: String, pageUrl: String) {
        runCatching {
            val requestCookies = httpClient.cookieJar.loadForRequest(pageUrl.toHttpUrl())
            log(
                sessionId,
                reason,
                "httpClientPreflightCookies count=${requestCookies.size} names=${requestCookies.joinToString(",") { it.name }}",
            )
            httpClient.newCall(buildPreflightRequest(pageUrl)).execute().use { response ->
                val selectedHeaders = listOf(
                    "server",
                    "cf-ray",
                    "cf-cache-status",
                    "content-type",
                    "location",
                    "set-cookie",
                ).joinToString(", ") { key ->
                    val value = response.header(key).orEmpty()
                    "$key=$value"
                }
                log(
                    sessionId,
                    reason,
                    "httpClientPreflight status=${response.code} message=${response.message} finalUrl=${response.request.url} headers=$selectedHeaders",
                )
            }
        }.onFailure { error ->
            log(sessionId, reason, "httpClientPreflight failed: ${error.message}", error)
        }
    }

    private fun log(sessionId: Int, reason: String, message: String, error: Throwable? = null) {
        val formatted = "[session=$sessionId][reason=$reason] $message"
        if (error == null) {
            Log.d(TAG, formatted)
        } else {
            Log.e(TAG, formatted, error)
        }
    }
}
