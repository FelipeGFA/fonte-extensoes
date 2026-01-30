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

    private fun d(s: String, k: Int = 0x1F): String = s.map { (it.code xor k).toChar() }.joinToString("")

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
        val isChapterPageRequest = targetEndpoint?.contains(d("\u0030\u006d\u007a\u007e\u007b")) == true
        val interfaceName = generateInterfaceName()
        val scriptWithInterface = getScriptWithInterface(interfaceName)

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }

                wv.webViewClient = object : WebViewClient() {
                    private var loginAttempted = false

                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (isLoginFlow) {
                            view?.evaluateJavascript(d("\u0073\u0070\u007c\u007e\u0073\u004c\u006b\u0070\u006d\u007e\u0078\u007a\u0031\u007c\u0073\u007a\u007e\u006d\u0037\u0036\u0024\u003f\u006c\u007a\u006c\u006c\u0076\u0070\u0071\u004c\u006b\u0070\u006d\u007e\u0078\u007a\u0031\u007c\u0073\u007a\u007e\u006d\u0037\u0036\u0024"), null)
                        }
                        view?.evaluateJavascript(d("\u0068\u0076\u0071\u007b\u0070\u0068\u0031\u0040\u0040\u0074\u006a\u006d\u0070\u005a\u0067\u006f\u007a\u007c\u006b\u004f\u007e\u0078\u007a\u006c\u003f\u0022\u003f") + isChapterPageRequest + ";", null)
                        view?.evaluateJavascript(scriptWithInterface, null)
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        if (loginCredentials != null && url.contains(d("\u0030\u0073\u0070\u0078\u0076\u0071")) && !loginAttempted) {
                            loginAttempted = true
                            Handler(Looper.getMainLooper()).postDelayed({ performAutoLogin(view, loginCredentials) }, 1500)
                        }
                    }
                }

                setupWebView(wv, jsInterface, isLoginFlow, interfaceName)
                wv.loadUrl(pageUrl)
            }.onFailure {
                latch.countDown()
            }
        }

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        handler.post { webView?.run { stopLoading(); destroy() } }

        return if (jsInterface.capturedData.isNotEmpty()) {
            ApiResponse(d("\u0075\u006c\u0070\u0071\u0032\u006f\u007e\u006d\u006c\u007a"), jsInterface.capturedData, d("\u0055\u004c\u0050\u0051\u0031\u006f\u007e\u006d\u006c\u007a"))
        } else {
            null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView, jsInterface: KuroJsInterface, clearCookies: Boolean, interfaceName: String) {
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
            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            databaseEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wv.addJavascriptInterface(jsInterface, interfaceName)
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

        private fun d(s: String, k: Int = 0x1F): String = s.map { (it.code xor k).toChar() }.joinToString("")

        @JavascriptInterface
        fun onDataParsed(data: String) {
            if (capturedData.isEmpty() && data.length > 50 && isValidData(data)) {
                capturedData = data
                latch.countDown()
            }
        }

        private fun isValidData(data: String): Boolean {
            if (data.contains(d("\u003d\u006c\u007a\u006d\u0069\u007a\u006d\u0040\u006b\u0076\u0072\u0076\u0071\u0078\u003d")) || data.contains(d("\u003d\u007c\u0079\u005c\u007e\u007c\u0077\u007a\u004c\u006b\u007e\u006b\u006a\u006c\u003d"))) {
                return false
            }
            return data.contains(d("\u003d\u007b\u007e\u006b\u007e\u003d")) || data.contains(d("\u003d\u006b\u0070\u0074\u007a\u0071\u003d")) ||
                data.contains(d("\u003d\u0072\u007e\u0071\u0078\u007e\u003d")) || data.contains(d("\u003d\u007c\u0077\u007e\u006f\u006b\u007a\u006d\u006c\u003d")) || data.contains(d("\u003d\u006f\u007e\u0078\u007a\u006c\u003d"))
        }
    }

    companion object {
        private const val TIMEOUT_SECONDS = 30L

        private fun d(s: String, k: Int = 0x1F): String = s.map { (it.code xor k).toChar() }.joinToString("")

        private fun generateInterfaceName(): String {
            val chars = d("\u007e\u007d\u007c\u007b\u007a\u0079\u0078\u0077\u0076\u0075\u0074\u0073\u0072\u0071\u0070\u006f\u006e\u006d\u006c\u006b\u006a\u0069\u0068\u0067\u0066\u0065")
            val random = java.util.Random(System.nanoTime())
            val prefix = (1..3).map { chars[random.nextInt(chars.length)] }.joinToString("")
            val suffix = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
            return "_${prefix}${System.currentTimeMillis() % 10000}${suffix}_"
        }

        private val webviewHookScript by lazy {
            KuroWebViewInterceptor::class.java.getResource(d("\u0030\u007e\u006c\u006c\u007a\u006b\u006c\u0030\u0068\u007a\u007d\u0069\u0076\u007a\u0068\u0032\u0077\u0070\u0070\u0074\u0031\u0075\u006c"))?.readText()
                ?: throw Exception(d("\u0068\u007a\u007d\u0069\u0076\u007a\u0068\u0032\u0077\u0070\u0070\u0074\u0031\u0075\u006c\u003f\u0071\u0070\u006b\u003f\u0079\u0070\u006a\u0071\u007b"))
        }

        fun getScriptWithInterface(interfaceName: String): String {
            return webviewHookScript.replace("__IFACE_PLACEHOLDER__", interfaceName)
        }
    }
}
