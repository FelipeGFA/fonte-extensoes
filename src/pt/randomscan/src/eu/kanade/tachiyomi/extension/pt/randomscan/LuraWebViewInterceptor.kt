package eu.kanade.tachiyomi.extension.pt.randomscan

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

class LuraWebViewInterceptor {

    data class ImageData(val index: Int, val data: String) // data is base64

    @SuppressLint("SetJavaScriptEnabled")
    fun extractChapterImages(
        chapterUrl: String,
        userAgent: String,
        cookies: String,
    ): List<String> {
        val handler = Handler(Looper.getMainLooper())
        val latch = CountDownLatch(1)
        val jsInterface = LuraJsInterface(latch)
        var webView: WebView? = null
        val interfaceName = generateInterfaceName()

        handler.post {
            runCatching {
                val wv = WebView(Injekt.get<Application>()).also { webView = it }

                setupWebView(wv, jsInterface, userAgent, cookies, interfaceName)

                wv.webViewClient = object : WebViewClient() {
                    private var scriptInjected = false

                    override fun onPageFinished(view: WebView, url: String) {
                        if (!scriptInjected) {
                            scriptInjected = true
                            // Wait for images to load, then extract
                            handler.postDelayed({
                                view.evaluateJavascript(getExtractionScript(interfaceName), null)
                            }, EXTRACTION_DELAY_MS,)
                        }
                    }
                }

                wv.loadUrl(chapterUrl)
            }.onFailure {
                latch.countDown()
            }
        }

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        handler.post { webView?.run { stopLoading(); destroy() } }

        return jsInterface.images.sortedBy { it.index }.map { it.data }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(
        wv: WebView,
        jsInterface: LuraJsInterface,
        userAgent: String,
        cookies: String,
        interfaceName: String,
    ) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(wv, true)
            // Set cookies for the domain
            cookies.split("; ").forEach { cookie ->
                setCookie("https://luratoons.net", cookie)
            }
            flush()
        }

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            blockNetworkImage = false
            userAgentString = userAgent
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wv.addJavascriptInterface(jsInterface, interfaceName)
    }

    private fun getExtractionScript(interfaceName: String): String {
        return """
            (async function() {
                const IFACE = window.$interfaceName;
                if (!IFACE) {
                    console.error('Interface not found');
                    return;
                }

                // Wait for reader div to be populated
                async function waitForImages(maxWait = 30000) {
                    const start = Date.now();
                    while (Date.now() - start < maxWait) {
                        const container = document.querySelector('.iAyij');
                        if (container) {
                            const imgs = container.querySelectorAll('img[src^="blob:"]');
                            if (imgs.length > 0) {
                                // Wait a bit more for all images to load
                                await new Promise(r => setTimeout(r, 2000));
                                return Array.from(imgs);
                            }
                        }
                        await new Promise(r => setTimeout(r, 500));
                    }
                    return [];
                }

                const images = await waitForImages();
                console.log('Found ' + images.length + ' images');

                if (images.length === 0) {
                    IFACE.onComplete('[]');
                    return;
                }

                const results = [];

                for (let i = 0; i < images.length; i++) {
                    const img = images[i];
                    try {
                        const response = await fetch(img.src);
                        const blob = await response.blob();
                        
                        const base64 = await new Promise((resolve, reject) => {
                            const reader = new FileReader();
                            reader.onloadend = () => resolve(reader.result);
                            reader.onerror = reject;
                            reader.readAsDataURL(blob);
                        });

                        results.push({
                            index: i,
                            data: base64
                        });

                        // Send progress update
                        IFACE.onProgress(i + 1, images.length);
                    } catch (e) {
                        console.error('Error extracting image ' + i + ': ' + e);
                    }
                }

                IFACE.onComplete(JSON.stringify(results));
            })();
        """.trimIndent()
    }

    private class LuraJsInterface(private val latch: CountDownLatch) {
        val images = mutableListOf<ImageData>()

        @JavascriptInterface
        fun onProgress(current: Int, total: Int) {
            // Log progress (optional)
        }

        @JavascriptInterface
        fun onComplete(jsonData: String) {
            try {
                val parsed = org.json.JSONArray(jsonData)
                for (i in 0 until parsed.length()) {
                    val obj = parsed.getJSONObject(i)
                    images.add(
                        ImageData(
                            index = obj.getInt("index"),
                            data = obj.getString("data"),
                        ),
                    )
                }
            } catch (e: Exception) {
                // Parse error
            }
            latch.countDown()
        }
    }

    companion object {
        private const val TIMEOUT_SECONDS = 120L // 2 minutes for large chapters
        private const val EXTRACTION_DELAY_MS = 3000L // Wait 3 seconds after page load

        private fun generateInterfaceName(): String {
            val chars = "abcdefghijklmnopqrstuvwxyz"
            val random = java.util.Random(System.nanoTime())
            val prefix = (1..3).map { chars[random.nextInt(chars.length)] }.joinToString("")
            val suffix = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
            return "_${prefix}${System.currentTimeMillis() % 10000}${suffix}_"
        }
    }
}
