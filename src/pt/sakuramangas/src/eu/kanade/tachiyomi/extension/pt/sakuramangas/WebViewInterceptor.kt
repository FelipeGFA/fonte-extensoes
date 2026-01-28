package eu.kanade.tachiyomi.extension.pt.sakuramangas

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.ByteArrayInputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebViewInterceptor(
    private val userAgent: String,
    private val httpClient: OkHttpClient,
    private val baseUrl: String,
) {
    private companion object {
        const val TIMEOUT_SECONDS = 30L
        const val STEALTH_PREFIX = "__SAKURA_STEALTH__:"
        val CLOUDFLARE_TITLES = listOf("just a moment", "cloudflare", "checking", "attention required")
        val IMAGE_URL_REGEX = Regex("/imagens/([a-f0-9]{32,})/(\\d{3})\\.(jpg|png|webp|gif)", RegexOption.IGNORE_CASE)
    }

    @Volatile private var isWebViewActive = false

    data class CapturedSecurityData(
        val mangaInfoJson: String?,
        val proof: String,
        val challenge: String,
        val mangaId: String,
        val csrfToken: String,
        val cookies: String,
    )

    data class ChapterData(
        val numPages: Int,
        val imageBaseUrl: String,
        val imageExtension: String,
    )

    private class StealthDataCapture(private val latch: CountDownLatch) {
        @Volatile var mangaInfoJson: String? = null

        @Volatile var proof: String? = null

        @Volatile var challenge: String? = null

        @Volatile var mangaId: String? = null

        @Volatile var csrfToken: String? = null

        @Volatile var cloudflareDetected = false

        fun processConsoleMessage(message: String): Boolean {
            if (!message.startsWith(STEALTH_PREFIX)) return false
            try {
                val json = JSONObject(message.removePrefix(STEALTH_PREFIX))
                when (json.optString("type")) {
                    "MANGA_INFO_REQUEST" -> handleMangaInfoRequest(json.optJSONObject("payload"))
                    "MANGA_INFO_RESPONSE" -> handleMangaInfoResponse(json.optJSONObject("payload"))
                }
            } catch (_: Exception) {}
            return true
        }

        private fun handleMangaInfoRequest(payload: JSONObject?) {
            val body = payload?.optString("body") ?: return
            if (body.isEmpty()) return

            val params = body.split("&").associate { param ->
                val parts = param.split("=", limit = 2)
                val key = java.net.URLDecoder.decode(parts[0], "UTF-8")
                val value = if (parts.size > 1) java.net.URLDecoder.decode(parts[1], "UTF-8") else ""
                key to value
            }

            params["proof"]?.takeIf { it.isNotEmpty() }?.let { proof = it }
            params["challenge"]?.takeIf { it.isNotEmpty() }?.let { challenge = it }
            params["manga_id"]?.takeIf { it.isNotEmpty() }?.let { mangaId = it }
            checkIfDataReady()
        }

        private fun handleMangaInfoResponse(payload: JSONObject?) {
            val data = payload?.optJSONObject("data") ?: return
            if (data.has("titulo")) {
                mangaInfoJson = data.toString()
                checkIfDataReady()
            }
        }

        private fun checkIfDataReady() {
            if (mangaInfoJson != null && proof != null && challenge != null && mangaId != null) {
                latch.countDown()
            }
        }

        fun isComplete() = mangaInfoJson != null && proof != null && challenge != null && mangaId != null
    }

    private class ChapterStealthCapture(private val latch: CountDownLatch) {
        @Volatile var imageHash: String? = null

        @Volatile var imageExtension: String? = null

        @Volatile var numPages: Int = 0

        @Volatile var cloudflareDetected = false

        fun processConsoleMessage(message: String): Boolean {
            if (!message.startsWith(STEALTH_PREFIX)) return false
            try {
                val json = JSONObject(message.removePrefix(STEALTH_PREFIX))
                if (json.optString("type") == "CHAPTER_DATA") {
                    val payload = json.optJSONObject("payload") ?: return true
                    val hash = payload.optString("hash")
                    val pages = payload.optInt("numPages", 0)
                    if (hash.isNotEmpty() && pages > 0) {
                        imageHash = hash
                        imageExtension = payload.optString("extension", "jpg")
                        numPages = pages
                        latch.countDown()
                    }
                }
            } catch (_: Exception) {}
            return true
        }

        fun updateFromInterception(hash: String, extension: String) {
            if (imageHash == null) {
                imageHash = hash
                imageExtension = extension
                if (numPages > 0) latch.countDown()
            }
        }

        fun isComplete() = imageHash != null && numPages > 0
    }

    private val hookScript: String by lazy {
        WebViewInterceptor::class.java.getResource("/assets/sakura-hook.js")?.readText()
            ?: throw Exception("sakura-hook.js not found")
    }

    private val chapterHookScript: String by lazy {
        WebViewInterceptor::class.java.getResource("/assets/sakura-chapter-hook.js")?.readText()
            ?: throw Exception("sakura-chapter-hook.js not found")
    }

    private val webdriverSpoofScript: String by lazy {
        WebViewInterceptor::class.java.getResource("/assets/webdriver-spoof.js")?.readText()
            ?: throw Exception("webdriver-spoof.js not found")
    }

    private data class CachedData(val data: CapturedSecurityData, val timestamp: Long)
    private data class CachedChapterData(val data: ChapterData, val timestamp: Long)

    private val cache = mutableMapOf<String, CachedData>()
    private val chapterCache = mutableMapOf<String, CachedChapterData>()
    private val cacheTtlMs = 30_000L
    private val chapterCacheTtlMs = 300_000L

    private fun normalizeUrl(url: String) = url.substringBefore("?").trimEnd('/')

    private fun getCachedData(url: String): CapturedSecurityData? {
        val key = normalizeUrl(url)
        val cached = cache[key] ?: return null
        return if (System.currentTimeMillis() - cached.timestamp < cacheTtlMs) cached.data else null.also { cache.remove(key) }
    }

    private fun getCachedChapterData(url: String): ChapterData? {
        val key = normalizeUrl(url)
        val cached = chapterCache[key] ?: return null
        return if (System.currentTimeMillis() - cached.timestamp < chapterCacheTtlMs) cached.data else null.also { chapterCache.remove(key) }
    }

    private fun getOkHttpCookies(): String {
        return runCatching {
            httpClient.cookieJar.loadForRequest(baseUrl.toHttpUrl())
                .joinToString("; ") { "${it.name}=${it.value}" }
        }.getOrDefault("")
    }

    private fun syncCookiesToWebView() {
        runCatching {
            val cookies = httpClient.cookieJar.loadForRequest(baseUrl.toHttpUrl())
            if (cookies.isEmpty()) return
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookies.forEach { cookie ->
                cookieManager.setCookie(baseUrl, "${cookie.name}=${cookie.value}; Domain=${cookie.domain}; Path=${cookie.path}")
            }
            cookieManager.flush()
        }
    }

    private fun isCloudflareTitle(title: String?) = title?.lowercase()?.let { t -> CLOUDFLARE_TITLES.any { t.contains(it) } } == true

    @SuppressLint("SetJavaScriptEnabled")
    fun captureSecurityData(pageUrl: String): CapturedSecurityData? {
        getCachedData(pageUrl)?.let { return it }

        if (isWebViewActive) {
            repeat(60) {
                Thread.sleep(500)
                getCachedData(pageUrl)?.let { return it }
                if (!isWebViewActive) return@repeat
            }
            if (isWebViewActive) return null
        }

        isWebViewActive = true
        try {
            return doCapture(pageUrl)
        } finally {
            isWebViewActive = false
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun doCapture(pageUrl: String): CapturedSecurityData? {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val stealthCapture = StealthDataCapture(latch)
        var webView: WebView? = null
        var hookInjected = false
        var spoofInjected = false

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }

                wv.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (!spoofInjected) { view?.evaluateJavascript(webdriverSpoofScript, null); spoofInjected = true }
                        if (!hookInjected) { view?.evaluateJavascript(hookScript, null); hookInjected = true }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (isCloudflareTitle(view?.title)) { stealthCapture.cloudflareDetected = true; latch.countDown(); return }
                        view?.evaluateJavascript(hookScript, null)
                    }

                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        val url = request?.url?.toString().orEmpty()

                        if (url.contains("manga_info")) {
                            request?.requestHeaders?.entries?.find { it.key.equals("X-CSRF-Token", ignoreCase = true) }?.value
                                ?.takeIf { it.isNotEmpty() }?.let { stealthCapture.csrfToken = it }
                        }

                        if (request?.isForMainFrame == true && url.contains(baseUrl) && !url.contains("block.php")) {
                            try {
                                val response = httpClient.newCall(buildDocumentRequest(url)).execute()
                                val body = response.body?.string() ?: return super.shouldInterceptRequest(view, request)
                                val injectionScript = "<script>$webdriverSpoofScript</script><script>$hookScript</script>"
                                val injectedHtml = body.replace("<head>", "<head>$injectionScript").replace("<HEAD>", "<HEAD>$injectionScript")
                                return WebResourceResponse(
                                    "text/html",
                                    "UTF-8",
                                    response.code,
                                    response.message.ifEmpty { "OK" },
                                    response.headers.toMultimap().mapValues { it.value.firstOrNull().orEmpty() },
                                    ByteArrayInputStream(injectedHtml.toByteArray(Charsets.UTF_8)),
                                )
                            } catch (_: Exception) {}
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                wv.webChromeClient = object : WebChromeClient() {
                    private var injected = false
                    override fun onConsoleMessage(msg: ConsoleMessage?) = stealthCapture.processConsoleMessage(msg?.message() ?: "") || super.onConsoleMessage(msg)
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (!injected && newProgress >= 5) { injected = true; view?.evaluateJavascript(webdriverSpoofScript, null); view?.evaluateJavascript(hookScript, null) }
                    }
                    override fun onReceivedTitle(view: WebView?, title: String?) { super.onReceivedTitle(view, title); if (isCloudflareTitle(title)) { stealthCapture.cloudflareDetected = true; latch.countDown() } }
                }

                setupWebView(wv, blockImages = true)
                syncCookiesToWebView()
                wv.loadUrl(pageUrl)
            }.onFailure { latch.countDown() }
        }

        val success = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        Thread.sleep(500)
        val cookies = getOkHttpCookies()
        handler.post { webView?.run { stopLoading(); destroy() } }

        if (stealthCapture.cloudflareDetected || !success || !stealthCapture.isComplete()) return null

        val result = CapturedSecurityData(
            mangaInfoJson = stealthCapture.mangaInfoJson,
            proof = stealthCapture.proof!!,
            challenge = stealthCapture.challenge!!,
            mangaId = stealthCapture.mangaId!!,
            csrfToken = stealthCapture.csrfToken.orEmpty(),
            cookies = cookies,
        )
        cache[normalizeUrl(pageUrl)] = CachedData(result, System.currentTimeMillis())
        return result
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getChapterData(pageUrl: String): ChapterData? {
        getCachedChapterData(pageUrl)?.let { return it }

        if (isWebViewActive) {
            repeat(60) {
                Thread.sleep(500)
                getCachedChapterData(pageUrl)?.let { return it }
                if (!isWebViewActive) return@repeat
            }
            if (isWebViewActive) return null
        }

        isWebViewActive = true
        try {
            return doChapterCapture(pageUrl)
        } finally {
            isWebViewActive = false
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun doChapterCapture(pageUrl: String): ChapterData? {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val stealthCapture = ChapterStealthCapture(latch)
        var webView: WebView? = null
        var hookInjected = false
        var spoofInjected = false

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }

                wv.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (!spoofInjected) { view?.evaluateJavascript(webdriverSpoofScript, null); spoofInjected = true }
                        if (!hookInjected) { view?.evaluateJavascript(chapterHookScript, null); hookInjected = true }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (isCloudflareTitle(view?.title)) { stealthCapture.cloudflareDetected = true; latch.countDown(); return }
                        view?.evaluateJavascript(chapterHookScript, null)
                    }

                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        val url = request?.url?.toString().orEmpty()

                        if (request?.isForMainFrame == true && url.contains(baseUrl) && !url.contains("block.php")) {
                            try {
                                val response = httpClient.newCall(buildDocumentRequest(url)).execute()
                                val body = response.body?.string() ?: return super.shouldInterceptRequest(view, request)
                                val injectionScript = "<script>$webdriverSpoofScript</script><script>$chapterHookScript</script>"
                                val injectedHtml = body.replace("<head>", "<head>$injectionScript").replace("<HEAD>", "<HEAD>$injectionScript")
                                return WebResourceResponse(
                                    "text/html",
                                    "UTF-8",
                                    response.code,
                                    response.message.ifEmpty { "OK" },
                                    response.headers.toMultimap().mapValues { it.value.firstOrNull().orEmpty() },
                                    ByteArrayInputStream(injectedHtml.toByteArray(Charsets.UTF_8)),
                                )
                            } catch (_: Exception) {}
                        }

                        if (url.contains("/imagens/")) {
                            IMAGE_URL_REGEX.find(url)?.let { stealthCapture.updateFromInterception(it.groupValues[1], it.groupValues[3].lowercase()) }
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                wv.webChromeClient = object : WebChromeClient() {
                    private var injected = false
                    override fun onConsoleMessage(msg: ConsoleMessage?) = stealthCapture.processConsoleMessage(msg?.message() ?: "") || super.onConsoleMessage(msg)
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (!injected && newProgress >= 5) { injected = true; view?.evaluateJavascript(webdriverSpoofScript, null); view?.evaluateJavascript(chapterHookScript, null) }
                    }
                    override fun onReceivedTitle(view: WebView?, title: String?) { super.onReceivedTitle(view, title); if (isCloudflareTitle(title)) { stealthCapture.cloudflareDetected = true; latch.countDown() } }
                }

                setupWebView(wv, blockImages = false)
                syncCookiesToWebView()
                wv.loadUrl(pageUrl)
            }.onFailure { latch.countDown() }
        }

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        handler.post { webView?.run { stopLoading(); destroy() } }

        if (stealthCapture.cloudflareDetected || !stealthCapture.isComplete()) return null

        val result = ChapterData(
            numPages = stealthCapture.numPages,
            imageBaseUrl = "$baseUrl/imagens/${stealthCapture.imageHash}/",
            imageExtension = stealthCapture.imageExtension ?: "jpg",
        )
        chapterCache[normalizeUrl(pageUrl)] = CachedChapterData(result, System.currentTimeMillis())
        return result
    }

    private fun buildDocumentRequest(url: String) = Request.Builder()
        .url(url)
        .header("User-Agent", userAgent)
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
        .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
        .header("Cache-Control", "no-cache")
        .header("DNT", "1")
        .header("Sec-CH-UA", "\"Not A(Brand\";v=\"8\", \"Chromium\";v=\"144\"")
        .header("Sec-CH-UA-Mobile", "?1")
        .header("Sec-CH-UA-Platform", "\"Android\"")
        .header("Sec-Fetch-Dest", "document")
        .header("Sec-Fetch-Mode", "navigate")
        .header("Sec-Fetch-Site", "none")
        .header("Sec-Fetch-User", "?1")
        .header("Upgrade-Insecure-Requests", "1")
        .build()

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView, blockImages: Boolean) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(wv, true)
        }
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = userAgent
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            loadsImagesAutomatically = !blockImages
            blockNetworkImage = blockImages
        }
    }
}
