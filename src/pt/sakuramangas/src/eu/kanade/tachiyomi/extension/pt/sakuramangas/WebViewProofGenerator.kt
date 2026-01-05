package eu.kanade.tachiyomi.extension.pt.sakuramangas

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Generates security proof using WebView.
 * Injects security.core.js script and executes generateHeaderProof function.
 */
class WebViewProofGenerator(
    private val baseUrl: String,
    private val userAgent: String,
) {
    private companion object {
        const val INTERFACE_NAME = "ProofBridge"
        const val TIMEOUT_SECONDS = 30L
    }

    /**
     * Generates the proof header using WebView to execute security.core.js
     *
     * @param challenge The base64 challenge from meta tag
     * @param key The numeric key (manga_info or chapter_read)
     * @param securityScript The security.core.js content
     * @param pageUrl The page URL to load in WebView for correct context
     * @return The proof hash or null if failed
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun generate(
        challenge: String,
        key: Long,
        securityScript: String,
        pageUrl: String?,
    ): String? {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val jsInterface = ProofJsInterface(latch)
        var webView: WebView? = null

        handler.post {
            try {
                val wv = WebView(Injekt.get<Application>())
                webView = wv

                android.webkit.CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(wv, true)
                }

                with(wv.settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    userAgentString = userAgent
                }

                wv.addJavascriptInterface(jsInterface, INTERFACE_NAME)

                wv.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        executeProofGeneration(view, securityScript, challenge, key)
                    }
                }

                wv.loadUrl(pageUrl ?: "$baseUrl/")
            } catch (_: Exception) {
                latch.countDown()
            }
        }

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        handler.post { webView?.destroy() }

        return jsInterface.proof?.takeIf { it.isNotEmpty() }
    }

    private fun executeProofGeneration(
        view: WebView,
        securityScript: String,
        challenge: String,
        key: Long,
    ) {
        val escapedScript = securityScript
            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("\$", "\\\$")

        val jsCode = """
            (function() {
                try {
                    eval(`$escapedScript`);
                    
                    var challenge = '$challenge';
                    var key = $key;
                    var genFunc = null;
                    
                    if (typeof J_3cf0d8 !== 'undefined' && typeof J_3cf0d8.generateHeaderProof === 'function') {
                        genFunc = J_3cf0d8.generateHeaderProof;
                    } else if (typeof window.generateHeaderProof === 'function') {
                        genFunc = window.generateHeaderProof;
                    } else if (typeof generateHeaderProof === 'function') {
                        genFunc = generateHeaderProof;
                    }
                    
                    if (genFunc) {
                        var result = genFunc(challenge, key);
                        
                        if (result && typeof result.then === 'function') {
                            result.then(function(proof) {
                                window.$INTERFACE_NAME.onProofGenerated(proof || '');
                            }).catch(function(e) {
                                window.$INTERFACE_NAME.onProofGenerated('');
                            });
                        } else {
                            window.$INTERFACE_NAME.onProofGenerated(result || '');
                        }
                    } else {
                        window.$INTERFACE_NAME.onProofGenerated('');
                    }
                } catch (e) {
                    window.$INTERFACE_NAME.onProofGenerated('');
                }
            })();
        """.trimIndent()

        view.evaluateJavascript(jsCode) {}
    }

    private class ProofJsInterface(private val latch: CountDownLatch) {
        var proof: String? = null
            private set

        @JavascriptInterface
        @Suppress("UNUSED")
        fun onProofGenerated(value: String) {
            proof = value.ifEmpty { null }
            latch.countDown()
        }
    }
}
