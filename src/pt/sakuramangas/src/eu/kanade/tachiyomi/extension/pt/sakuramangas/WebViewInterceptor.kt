package eu.kanade.tachiyomi.extension.pt.sakuramangas

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebViewInterceptor(
    private val userAgent: String,
    private val httpClient: OkHttpClient,
    private val baseUrl: String,
) {
    private companion object {
        const val TIMEOUT_SECONDS = 30L
        val CLOUDFLARE_TITLES = listOf("just a moment", "cloudflare", "checking", "attention required")
        val IMAGE_URL_REGEX = Regex("/imagens/([a-f0-9]{32,})/(\\d{3})\\.(jpg|png|webp|gif)", RegexOption.IGNORE_CASE)
    }

    @Volatile
    private var isWebViewActive = false

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

    private class SakuraJsInterface(private val latch: CountDownLatch) {
        @Volatile var mangaInfoJson: String? = null

        @Volatile var chapterPostBody: String? = null

        @Volatile var csrfToken: String? = null

        @Volatile var cloudflareDetected = false

        @JavascriptInterface
        fun onHookReady() {}

        @JavascriptInterface
        fun onMangaInfo(jsonData: String, url: String) {
            mangaInfoJson = jsonData
            checkIfDataReady()
        }

        @JavascriptInterface
        fun onChapterPostData(body: String, token: String, url: String) {
            chapterPostBody = body
            csrfToken = token.ifEmpty { null }
            checkIfDataReady()
        }

        @JavascriptInterface
        fun onError(error: String) {}

        private fun checkIfDataReady() {
            if (mangaInfoJson != null && chapterPostBody != null) {
                latch.countDown()
            }
        }

        fun parsePostBody(): Triple<String, String, String>? {
            val body = chapterPostBody ?: return null
            val params = body.split("&").associate { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
            }
            val proof = params["proof"] ?: return null
            val challenge = params["challenge"] ?: return null
            val mangaId = params["manga_id"] ?: return null
            return Triple(proof, challenge, mangaId)
        }
    }

    private val hookScript: String by lazy {
        WebViewInterceptor::class.java.getResource("/assets/sakura-hook.js")?.readText()
            ?: throw Exception("sakura-hook.js not found in assets")
    }

    private data class CachedData(val securityData: CapturedSecurityData, val timestamp: Long)

    private val cache = mutableMapOf<String, CachedData>()
    private val cacheTtlMs = 30 * 1000L

    private fun normalizeUrl(url: String) = url.substringBefore("?").trimEnd('/')

    private fun getCachedData(url: String): CapturedSecurityData? {
        val key = normalizeUrl(url)
        val cached = cache[key] ?: return null
        val age = System.currentTimeMillis() - cached.timestamp
        return if (age < cacheTtlMs) cached.securityData else null.also { cache.remove(key) }
    }

    private fun cacheData(url: String, data: CapturedSecurityData) {
        cache[normalizeUrl(url)] = CachedData(data, System.currentTimeMillis())
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
                val cookieString = "${cookie.name}=${cookie.value}; Domain=${cookie.domain}; Path=${cookie.path}"
                cookieManager.setCookie(baseUrl, cookieString)
            }
            cookieManager.flush()
        }
    }

    private fun isCloudflareTitle(title: String?): Boolean {
        if (title.isNullOrBlank()) return false
        val lowerTitle = title.lowercase()
        return CLOUDFLARE_TITLES.any { lowerTitle.contains(it) }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun captureSecurityData(pageUrl: String): CapturedSecurityData? {
        getCachedData(pageUrl)?.let { return it }

        if (isWebViewActive) {
            var attempts = 0
            while (isWebViewActive && attempts < 60) {
                Thread.sleep(500)
                attempts++
                getCachedData(pageUrl)?.let { return it }
            }
            if (isWebViewActive) return null
        }

        return captureSecurityDataInternal(pageUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun captureSecurityDataInternal(pageUrl: String): CapturedSecurityData? {
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
        val jsInterface = SakuraJsInterface(latch)
        var webView: WebView? = null
        var hookInjected = false

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }

                wv.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (!hookInjected) {
                            view?.evaluateJavascript(hookScript, null)
                            hookInjected = true
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (isCloudflareTitle(view?.title)) {
                            jsInterface.cloudflareDetected = true
                            latch.countDown()
                            return
                        }
                        view?.evaluateJavascript(hookScript, null)
                    }

                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                wv.webChromeClient = object : WebChromeClient() {
                    private var scriptInjectedViaProgress = false

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (!scriptInjectedViaProgress && newProgress >= 10) {
                            scriptInjectedViaProgress = true
                            view?.evaluateJavascript(hookScript, null)
                        }
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        if (isCloudflareTitle(title)) {
                            jsInterface.cloudflareDetected = true
                            latch.countDown()
                        }
                    }
                }

                setupWebView(wv, jsInterface)
                syncCookiesToWebView()
                wv.loadUrl(pageUrl)
            }.onFailure { latch.countDown() }
        }

        val success = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        val cookies = getOkHttpCookies()

        handler.post {
            webView?.run {
                stopLoading()
                destroy()
            }
        }

        if (jsInterface.cloudflareDetected || !success || jsInterface.mangaInfoJson == null) {
            return null
        }

        val (proof, challenge, mangaId) = jsInterface.parsePostBody() ?: return null

        val result = CapturedSecurityData(
            mangaInfoJson = jsInterface.mangaInfoJson,
            proof = proof,
            challenge = challenge,
            mangaId = mangaId,
            csrfToken = jsInterface.csrfToken.orEmpty(),
            cookies = cookies,
        )

        cacheData(pageUrl, result)
        return result
    }

    private class ChapterJsInterface(private val latch: CountDownLatch) {
        @Volatile var imageHash: String? = null

        @Volatile var imageExtension: String? = null

        @Volatile var numPages: Int = 0

        @Volatile var cloudflareDetected = false

        @JavascriptInterface
        fun onHookReady() {}

        @JavascriptInterface
        fun onChapterData(hash: String, extension: String, pages: Int) {
            imageHash = hash
            imageExtension = extension
            numPages = pages
            latch.countDown()
        }

        @JavascriptInterface
        fun onError(error: String) {}
    }

    private val chapterHookScript: String by lazy {
        WebViewInterceptor::class.java.getResource("/assets/sakura-chapter-hook.js")?.readText()
            ?: throw Exception("sakura-chapter-hook.js not found in assets")
    }

    private data class CachedChapterData(val data: ChapterData, val timestamp: Long)

    private val chapterCache = mutableMapOf<String, CachedChapterData>()
    private val chapterCacheTtlMs = 5 * 60 * 1000L

    private fun getCachedChapterData(url: String): ChapterData? {
        val key = normalizeUrl(url)
        val cached = chapterCache[key] ?: return null
        val age = System.currentTimeMillis() - cached.timestamp
        return if (age < chapterCacheTtlMs) cached.data else null.also { chapterCache.remove(key) }
    }

    private fun cacheChapterData(url: String, data: ChapterData) {
        chapterCache[normalizeUrl(url)] = CachedChapterData(data, System.currentTimeMillis())
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getChapterData(pageUrl: String): ChapterData? {
        getCachedChapterData(pageUrl)?.let { return it }

        if (isWebViewActive) {
            var attempts = 0
            while (isWebViewActive && attempts < 60) {
                Thread.sleep(500)
                attempts++
                getCachedChapterData(pageUrl)?.let { return it }
            }
            if (isWebViewActive) return null
        }

        return getChapterDataInternal(pageUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun getChapterDataInternal(pageUrl: String): ChapterData? {
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
        val jsInterface = ChapterJsInterface(latch)
        var webView: WebView? = null
        var hookInjected = false
        var interceptedHash: String? = null
        var interceptedExtension: String? = null

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }

                wv.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (!hookInjected) {
                            view?.evaluateJavascript(chapterHookScript, null)
                            hookInjected = true
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (isCloudflareTitle(view?.title)) {
                            jsInterface.cloudflareDetected = true
                            latch.countDown()
                            return
                        }
                        view?.evaluateJavascript(chapterHookScript, null)
                    }

                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        val url = request?.url?.toString().orEmpty()

                        if (interceptedHash == null && url.contains("/imagens/")) {
                            IMAGE_URL_REGEX.find(url)?.let { match ->
                                interceptedHash = match.groupValues[1]
                                interceptedExtension = match.groupValues[3].lowercase()

                                if (jsInterface.numPages > 0) {
                                    jsInterface.imageHash = interceptedHash
                                    jsInterface.imageExtension = interceptedExtension
                                    latch.countDown()
                                }
                            }
                        }

                        return super.shouldInterceptRequest(view, request)
                    }
                }

                wv.webChromeClient = object : WebChromeClient() {
                    private var scriptInjectedViaProgress = false

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (!scriptInjectedViaProgress && newProgress >= 10) {
                            scriptInjectedViaProgress = true
                            view?.evaluateJavascript(chapterHookScript, null)
                        }
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        if (isCloudflareTitle(title)) {
                            jsInterface.cloudflareDetected = true
                            latch.countDown()
                        }
                    }
                }

                setupChapterWebView(wv, jsInterface)
                syncCookiesToWebView()
                wv.loadUrl(pageUrl)
            }.onFailure { latch.countDown() }
        }

        val success = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        handler.post {
            webView?.run {
                stopLoading()
                destroy()
            }
        }

        if (jsInterface.cloudflareDetected) return null

        val finalHash = jsInterface.imageHash ?: interceptedHash
        val finalExtension = jsInterface.imageExtension ?: interceptedExtension ?: "jpg"
        val finalNumPages = jsInterface.numPages

        if (finalHash == null || finalNumPages <= 0) return null

        val result = ChapterData(
            numPages = finalNumPages,
            imageBaseUrl = "$baseUrl/imagens/$finalHash/",
            imageExtension = finalExtension,
        )

        cacheChapterData(pageUrl, result)
        return result
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupChapterWebView(wv: WebView, jsInterface: ChapterJsInterface) {
        setupCommonWebViewSettings(wv)
        wv.settings.apply {
            loadsImagesAutomatically = true
            blockNetworkImage = false
        }
        wv.addJavascriptInterface(jsInterface, "SakuraChapterInterface")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView, jsInterface: SakuraJsInterface) {
        setupCommonWebViewSettings(wv)
        wv.settings.apply {
            loadsImagesAutomatically = false
            blockNetworkImage = true
        }
        wv.addJavascriptInterface(jsInterface, "SakuraInterface")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupCommonWebViewSettings(wv: WebView) {
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
        }
    }
}
