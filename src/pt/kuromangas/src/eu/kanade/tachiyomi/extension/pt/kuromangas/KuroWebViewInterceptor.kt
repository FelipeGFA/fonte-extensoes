package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KuroWebViewInterceptor {

    data class ApiResponse(val url: String, val data: String, val approach: String)
    data class LoginCredentials(val email: String, val password: String)

    @SuppressLint("SetJavaScriptEnabled")
    fun getDecryptedData(
        pageUrl: String,
        targetEndpoint: String? = null,
        loginCredentials: LoginCredentials? = null,
    ): ApiResponse? {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val jsInterface = KuroJsInterface(latch)
        var webView: WebView? = null
        val isLoginFlow = loginCredentials != null
        val isChapterPageRequest = targetEndpoint?.contains("/read") == true

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }

                wv.webViewClient = object : WebViewClient() {
                    private var loginAttempted = false

                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (isLoginFlow) {
                            view?.evaluateJavascript("localStorage.clear(); sessionStorage.clear();", null)
                        }
                        view?.evaluateJavascript("window.__kuroExpectPages = $isChapterPageRequest;", null)
                        view?.evaluateJavascript(webviewHookScript, null)
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        if (loginCredentials != null && url.contains("/login") && !loginAttempted) {
                            loginAttempted = true
                            Handler(Looper.getMainLooper()).postDelayed({ performAutoLogin(view, loginCredentials) }, 1500)
                        }
                    }
                }

                setupWebView(wv, jsInterface, isLoginFlow)
                wv.loadUrl(pageUrl)
            }.onFailure {
                latch.countDown()
            }
        }

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        handler.post { webView?.run { stopLoading(); destroy() } }

        return if (jsInterface.capturedData.isNotEmpty()) {
            ApiResponse("json-parse", jsInterface.capturedData, "JSON.parse")
        } else {
            null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView, jsInterface: KuroJsInterface, clearCookies: Boolean) {
        CookieManager.getInstance().apply {
            if (clearCookies) {
                removeAllCookies(null)
                flush()
            }
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(wv, true)
        }

        wv.clearCache(true)
        wv.clearHistory()

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = false
            blockNetworkImage = true
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            databaseEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wv.addJavascriptInterface(jsInterface, INTERFACE_NAME)
    }

    private fun performAutoLogin(webView: WebView, credentials: LoginCredentials) {
        val script = """
            (function() {
                function setInput(input, value) {
                    if (!input) return;
                    var setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                    setter.call(input, value);
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                }
                var email = document.querySelector('input[type="email"]');
                var pass = document.querySelector('input[type="password"]');
                var btn = document.querySelector('button[type="submit"]');
                if (email && pass && btn) {
                    setInput(email, '${credentials.email.replace("'", "\\'")}');
                    setInput(pass, '${credentials.password.replace("'", "\\'")}');
                    setTimeout(function() { btn.click(); }, 500);
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    private class KuroJsInterface(private val latch: CountDownLatch) {
        @Volatile var capturedData = ""

        @JavascriptInterface
        fun onDataParsed(data: String) {
            if (capturedData.isEmpty() && data.length > 50 && isValidData(data)) {
                capturedData = data
                latch.countDown()
            }
        }

        private fun isValidData(data: String): Boolean {
            if (data.contains("\"server_timing\"") || data.contains("\"cfCacheStatus\"")) {
                return false
            }
            return data.contains("\"data\"") || data.contains("\"token\"") ||
                data.contains("\"manga\"") || data.contains("\"chapters\"") || data.contains("\"pages\"")
        }
    }

    companion object {
        private const val TIMEOUT_SECONDS = 30L
        private const val INTERFACE_NAME = "__dataHandler"

        private val webviewHookScript by lazy {
            KuroWebViewInterceptor::class.java.getResource("/assets/webview-hook.js")?.readText()
                ?.replace("__interface__", INTERFACE_NAME)
                ?: throw Exception("webview-hook.js not found")
        }
    }
}
