package eu.kanade.tachiyomi.extension.pt.randomscan

import android.webkit.CookieManager
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.lib.dataimage.DataImageInterceptor
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.getPreferencesLazy
import keiyoushi.utils.parseAs
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Locale

class LuraToon : HttpSource(), ConfigurableSource {

    override val name = "Lura Toon"

    override val baseUrl = "https://luratoons.net"

    override val lang = "pt-BR"

    override val supportsLatest = true

    private val preferences by getPreferencesLazy()

    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val host = url.host
            cookieStore.getOrPut(host) { mutableListOf() }.apply {
                cookies.forEach { cookie ->
                    removeAll { it.name == cookie.name }
                    add(cookie)
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host]?.filter { it.matches(url) } ?: emptyList()
        }
    }

    private val loginClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .followRedirects(false)
            .build()
    }

    override val client by lazy {
        network.cloudflareClient.newBuilder()
            .rateLimit(2)
            .cookieJar(cookieJar)
            .addInterceptor(DataImageInterceptor())
            .addInterceptor { chain ->
                val request = chain.request()

                ensureLoggedIn()

                // Add X-CSRFToken for POST requests
                if (request.method == "POST") {
                    val csrfToken = cookieStore[baseUrl.toHttpUrl().host]
                        ?.find { it.name == "csrftoken" }?.value ?: ""

                    if (csrfToken.isNotEmpty()) {
                        val newRequest = request.newBuilder()
                            .header("X-CSRFToken", csrfToken)
                            .build()
                        return@addInterceptor chain.proceed(newRequest)
                    }
                }

                chain.proceed(request)
            }
            .build()
    }

    @Volatile
    private var isLoggedIn = false

    @Synchronized
    private fun ensureLoggedIn() {
        if (isLoggedIn) return

        val email = preferences.getString(PREF_EMAIL, "") ?: ""
        val password = preferences.getString(PREF_PASSWORD, "") ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            throw Exception("Credenciais não configuradas. Vá em Configurações da Fonte e configure Email e Senha.")
        }

        // Step 1: Get login page to obtain csrftoken cookie
        val loginPageRequest = GET("$baseUrl/accounts/login/")
        val loginPageResponse = loginClient.newCall(loginPageRequest).execute()
        loginPageResponse.close()

        val csrfToken = cookieStore[baseUrl.toHttpUrl().host]
            ?.find { it.name == "csrftoken" }?.value
            ?: throw Exception("Não foi possível obter o token CSRF")

        // Step 2: POST login with credentials
        val loginBody = FormBody.Builder()
            .add("csrfmiddlewaretoken", csrfToken)
            .add("login", email)
            .add("password", password)
            .build()

        val loginHeaders = Headers.Builder()
            .add("Content-Type", "application/x-www-form-urlencoded")
            .add("Referer", "$baseUrl/accounts/login/")
            .add("Origin", baseUrl)
            .build()

        val loginRequest = Request.Builder()
            .url("$baseUrl/accounts/login/")
            .headers(loginHeaders)
            .post(loginBody)
            .build()

        val loginResponse = loginClient.newCall(loginRequest).execute()

        val statusCode = loginResponse.code
        loginResponse.close()

        if (statusCode !in listOf(302, 200)) {
            throw Exception("Login falhou - código de status: $statusCode")
        }

        val sessionId = cookieStore[baseUrl.toHttpUrl().host]
            ?.find { it.name == "sessionid" }?.value

        if (sessionId.isNullOrEmpty()) {
            throw Exception("Login falhou - credenciais inválidas")
        }

        syncCookiesToWebView()

        isLoggedIn = true
    }

    private fun syncCookiesToWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieStore[baseUrl.toHttpUrl().host]?.forEach { cookie ->
            cookieManager.setCookie(baseUrl, "${cookie.name}=${cookie.value}")
        }
        cookieManager.flush()
    }

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("Origin", baseUrl)
        .add("Referer", "$baseUrl/")

    // ============================== Popular ===============================

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/api/main/", headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val result = response.parseAs<MainPageDto>()
        val mangas = result.top10.map { it.toSManga(baseUrl) }
        return MangasPage(mangas, hasNextPage = false)
    }

    // ============================== Latest ================================

    override fun latestUpdatesRequest(page: Int): Request {
        return if (page == 1) {
            GET("$baseUrl/api/main/", headers)
        } else {
            POST("$baseUrl/api/main/?part=1", headers, FormBody.Builder().build())
        }
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        val result = response.parseAs<MainPageDto>()
        val mangas = result.lancamentos.map { it.toSManga(baseUrl) }
        // Only 2 pages: initial GET and ?part=1
        val hasNextPage = response.request.url.queryParameter("part") == null && mangas.isNotEmpty()
        return MangasPage(mangas, hasNextPage)
    }

    // ============================== Search ================================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query.length < 3) {
            throw Exception("A pesquisa deve ter pelo menos 3 caracteres")
        }

        val url = "$baseUrl/api/autocomplete/$query/"
        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val result = response.parseAs<SearchResultDto>()
        val mangas = result.obras.map { it.toSManga(baseUrl) }
        return MangasPage(mangas, hasNextPage = false)
    }

    // ============================== Details ===============================

    override fun getMangaUrl(manga: SManga): String = "$baseUrl${manga.url}"

    override fun mangaDetailsRequest(manga: SManga): Request {
        val slug = manga.url.removePrefix("/").removeSuffix("/")
        return GET("$baseUrl/api/obra/$slug/", headers)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        return response.parseAs<MangaDetailsDto>().toSManga(baseUrl)
    }

    // ============================== Chapters ==============================

    override fun chapterListRequest(manga: SManga): Request = mangaDetailsRequest(manga)

    override fun chapterListParse(response: Response): List<SChapter> {
        val manga = response.parseAs<MangaDetailsDto>()
        return manga.caps.map { it.toSChapter(manga.slug) }
            .sortedByDescending { it.chapter_number }
    }

    // ============================== Pages =================================

    private val webViewInterceptor by lazy { LuraWebViewInterceptor() }

    override fun getChapterUrl(chapter: SChapter): String = "$baseUrl${chapter.url}"

    override fun pageListRequest(chapter: SChapter): Request {
        // This request is used to build the chapter URL for WebView
        // We won't actually use the API response
        return GET("$baseUrl${chapter.url}", headers)
    }

    override fun pageListParse(response: Response): List<Page> {
        // Close the unused response
        response.close()

        // Get the chapter URL for WebView extraction
        val chapterUrl = response.request.url.toString()

        // Build cookies string for WebView
        val cookies = cookieStore[baseUrl.toHttpUrl().host]
            ?.joinToString("; ") { "${it.name}=${it.value}" } ?: ""

        // Use WebView to load the page and extract decrypted images
        val base64Images = webViewInterceptor.extractChapterImages(
            chapterUrl = chapterUrl,
            userAgent = headers["User-Agent"] ?: DEFAULT_USER_AGENT,
            cookies = cookies,
        )

        if (base64Images.isEmpty()) {
            throw Exception("Não foi possível extrair as imagens do capítulo. Certifique-se de estar logado.")
        }

        // Convert data:image URLs to format expected by DataImageInterceptor
        // Input: data:image/webp;base64,RIFF...
        // Output: https://127.0.0.1/?image/webp;base64,RIFF...
        return base64Images.mapIndexed { index, base64Data ->
            val imageUrl = if (base64Data.startsWith("data:")) {
                "https://127.0.0.1/?" + base64Data.substringAfter("data:")
            } else {
                base64Data
            }
            Page(index, imageUrl = imageUrl)
        }
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException()
    }

    override fun imageRequest(page: Page): Request {
        val imageUrl = page.imageUrl!!
        val headers = headersBuilder()
            .add("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .build()
        return GET(imageUrl, headers)
    }

    // ============================== Filters ===============================

    override fun getFilterList(): FilterList = FilterList()

    // ============================== Preferences ===========================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        EditTextPreference(screen.context).apply {
            key = PREF_EMAIL
            title = "Email"
            summary = "Email de login para acessar conteúdo restrito"
            dialogTitle = "Email"
            setDefaultValue("")
        }.let(screen::addPreference)

        EditTextPreference(screen.context).apply {
            key = PREF_PASSWORD
            title = "Senha"
            summary = "Senha de login para acessar conteúdo restrito"
            dialogTitle = "Senha"
            setDefaultValue("")
        }.let(screen::addPreference)
    }

    companion object {
        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)

        private const val PREF_EMAIL = "pref_email"
        private const val PREF_PASSWORD = "pref_password"
        private const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
    }
}
