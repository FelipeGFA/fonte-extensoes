package eu.kanade.tachiyomi.extension.pt.sakuramangas

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * WebView Interceptor for SakuraMangas.
 *
 * Captures proof/challenge from jQuery $.ajax POST requests via JavaScript hook.
 * Inspired by MangaFire/Comikey/Koharu WebView patterns.
 */
class WebViewInterceptor(
    private val userAgent: String,
    private val httpClient: OkHttpClient,
    private val baseUrl: String,
) {
    data class ProofData(val proof: String, val challenge: String)

    data class ChapterData(
        val numPages: Int,
        val imageBaseUrl: String,
        val imageExtension: String,
    )

    private companion object {
        const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes
        const val WEBVIEW_TIMEOUT_SECONDS = 35L

        val IMAGE_PATTERN = Regex("""(.*/imagens/[^/]+/)(\d{3})\.(webp|jpg|png)""")

        val BROWSER_HEADERS = mapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
            "Accept-Language" to "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7",
            "Sec-Fetch-Dest" to "document",
            "Sec-Fetch-Mode" to "navigate",
            "Sec-Fetch-Site" to "none",
            "Upgrade-Insecure-Requests" to "1",
        )
    }

    // Scripts loaded from assets
    private val proofHookScript by lazy {
        javaClass.getResource("/assets/proof-hook.js")?.readText()
            ?: throw Exception("Script proof-hook.js não encontrado")
    }

    private val chapterHookScript by lazy {
        javaClass.getResource("/assets/chapter-hook.js")?.readText()
            ?: throw Exception("Script chapter-hook.js não encontrado")
    }

    // ======================== PROOF CACHE ========================

    private object ProofCache {
        private var cachedProof: ProofData? = null
        private var cachedBaseUrl: String? = null
        private var cacheTime: Long = 0

        @Synchronized
        fun get(url: String): ProofData? {
            val urlBase = extractBaseUrl(url)
            return if (cachedBaseUrl == urlBase && System.currentTimeMillis() - cacheTime < CACHE_TTL_MS) {
                cachedProof
            } else {
                null
            }
        }

        @Synchronized
        fun set(url: String, proof: ProofData) {
            cachedBaseUrl = extractBaseUrl(url)
            cachedProof = proof
            cacheTime = System.currentTimeMillis()
        }

        private fun extractBaseUrl(url: String): String =
            url.toHttpUrl().let { "${it.scheme}://${it.host}" }
    }

    // ======================== WEBVIEW MUTEX ========================

    private val webViewMutex = Semaphore(1)

    // ======================== COOKIE SYNC ========================

    private fun syncCookiesToWebView() {
        val cookies = httpClient.cookieJar.loadForRequest(baseUrl.toHttpUrl())
        if (cookies.isEmpty()) return

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        cookies.forEach { cookie ->
            cookieManager.setCookie(
                baseUrl,
                "${cookie.name}=${cookie.value}; Domain=${cookie.domain}; Path=${cookie.path}",
            )
        }
        cookieManager.flush()
    }

    // ======================== GET PROOF ========================

    @SuppressLint("SetJavaScriptEnabled")
    fun getProof(pageUrl: String): ProofData? {
        // Check cache first
        ProofCache.get(pageUrl)?.let { return it }

        // Wait for mutex (up to 40s to allow another thread to complete)
        if (!webViewMutex.tryAcquire(40, TimeUnit.SECONDS)) {
            return ProofCache.get(pageUrl)
        }

        try {
            // Double-check cache after acquiring mutex
            ProofCache.get(pageUrl)?.let { return it }
            return executeProofCapture(pageUrl)
        } finally {
            webViewMutex.release()
        }
    }

    private fun executeProofCapture(pageUrl: String): ProofData? {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val jsInterface = ProofJsInterface(latch)
        var webView: WebView? = null
        var isCloudflareChallenge = false

        handler.post {
            val wv = WebView(Injekt.get<Application>()).also { webView = it }
            setupWebView(wv, jsInterface)

            wv.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    view.evaluateJavascript(proofHookScript, null)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    view.evaluateJavascript(
                        "(function(){return document.title.indexOf('momento')!==-1||document.body.innerHTML.indexOf('challenge-platform')!==-1})()",
                    ) { result ->
                        if (result == "true") {
                            isCloudflareChallenge = true
                            latch.countDown()
                        }
                    }
                }
            }

            syncCookiesToWebView()
            wv.loadUrl(pageUrl, BROWSER_HEADERS)
        }

        latch.await(WEBVIEW_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        handler.post {
            webView?.stopLoading()
            webView?.destroy()
        }

        if (isCloudflareChallenge) return null

        val proof = jsInterface.proof
        val challenge = jsInterface.challenge

        return if (proof.isNotEmpty() && challenge.isNotEmpty()) {
            ProofData(proof, challenge).also { ProofCache.set(pageUrl, it) }
        } else {
            null
        }
    }

    // ======================== GET CHAPTER DATA ========================

    @SuppressLint("SetJavaScriptEnabled")
    fun getChapterData(chapterUrl: String): ChapterData? {
        if (!webViewMutex.tryAcquire(40, TimeUnit.SECONDS)) return null

        try {
            return executeChapterCapture(chapterUrl)
        } finally {
            webViewMutex.release()
        }
    }

    private fun executeChapterCapture(chapterUrl: String): ChapterData? {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val jsInterface = ChapterJsInterface(latch)
        var webView: WebView? = null
        var capturedImageBase: String? = null
        var capturedImageExt: String? = null

        handler.post {
            val wv = WebView(Injekt.get<Application>()).also { webView = it }
            setupWebView(wv, jsInterface)

            wv.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    view.evaluateJavascript(chapterHookScript, null)
                }

                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    val url = request?.url?.toString() ?: return null

                    if (capturedImageBase == null) {
                        IMAGE_PATTERN.find(url)?.let { match ->
                            val (base, _, ext) = match.destructured
                            capturedImageBase = base
                            capturedImageExt = ext
                            jsInterface.setImageCaptured()
                        }
                    }
                    return null
                }
            }

            syncCookiesToWebView()
            wv.loadUrl(chapterUrl, BROWSER_HEADERS)
        }

        latch.await(WEBVIEW_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        handler.post {
            webView?.stopLoading()
            webView?.destroy()
        }

        val imageBase = capturedImageBase ?: return null
        val imageExt = capturedImageExt ?: return null
        val numPages = jsInterface.numPages.takeIf { it > 0 } ?: return null

        return ChapterData(
            numPages = numPages,
            imageBaseUrl = imageBase,
            imageExtension = imageExt,
        )
    }

    // ======================== WEBVIEW SETUP ========================

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView, jsInterface: Any) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(wv, true)
        }

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            userAgentString = userAgent
            blockNetworkImage = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        wv.addJavascriptInterface(jsInterface, "Interceptor")
    }

    // ======================== JS INTERFACES ========================

    private class ProofJsInterface(private val latch: CountDownLatch) {
        @Volatile var proof = ""

        @Volatile var challenge = ""

        @JavascriptInterface
        fun onProof(proof: String, challenge: String) {
            if (this.proof.isEmpty() && proof.isNotEmpty() && challenge.isNotEmpty()) {
                this.proof = proof
                this.challenge = challenge
                latch.countDown()
            }
        }
    }

    private class ChapterJsInterface(private val latch: CountDownLatch) {
        @Volatile var numPages = 0

        @Volatile private var imageCaptured = false

        @JavascriptInterface
        fun onNumPages(pages: Int) {
            if (numPages == 0 && pages > 0) {
                numPages = pages
                checkComplete()
            }
        }

        fun setImageCaptured() {
            imageCaptured = true
            checkComplete()
        }

        private fun checkComplete() {
            if (numPages > 0 && imageCaptured) {
                latch.countDown()
            }
        }
    }
}
