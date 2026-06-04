package eu.kanade.tachiyomi.extension.pt.blackoutcomics

import android.content.SharedPreferences
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.lib.cookieinterceptor.CookieInterceptor
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.getPreferencesLazy
import keiyoushi.utils.parseAs
import keiyoushi.utils.tryParse
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Element
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class BlackoutComics :
    HttpSource(),
    ConfigurableSource {

    override val name = "Blackout Comics"

    override val baseUrl = "https://blackoutcomics.com"

    override val lang = "pt-BR"

    override val supportsLatest = true

    private val preferences: SharedPreferences by getPreferencesLazy()

    private var loginState = LoginState.UNCHECKED

    private val ageGateInterceptor = CookieInterceptor(
        baseUrl.toHttpUrl().host,
        listOf(
            "_popprepop" to "1",
            "age_gate_consent" to ageGateConsentValue(),
        ),
    )

    override val client: OkHttpClient = network.client.newBuilder()
        .addInterceptor(ageGateInterceptor)
        .addInterceptor(::imageRetryInterceptor)
        .build()

    override fun headersBuilder() = super.headersBuilder()
        .add("DNT", "1")
        .add("Sec-GPC", "1")
        .add("Upgrade-Insecure-Requests", "1")

    override fun getMangaUrl(manga: SManga): String = manga.url.toAbsoluteUrl()

    override fun getChapterUrl(chapter: SChapter): String = chapter.url.toAbsoluteUrl()

    // ============================== Popular ===============================
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/ranking", headers)

    override fun popularMangaParse(response: Response): MangasPage = response.use {
        val doc = it.asJsoup()
        val mangas = doc.select(".ranking-grid a.webtoon-card").mapNotNull { it.toSManga() }
        MangasPage(mangas, false)
    }

    // =============================== Latest ===============================
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/atualizados-recente?page=$page", headers)

    override fun latestUpdatesParse(response: Response): MangasPage = response.use {
        val doc = it.asJsoup()
        val mangas = doc.select(".webtoon-grid a.webtoon-card").mapNotNull { it.toSManga() }
        val hasNext = doc.select(".pagerx__link[rel=next]").isNotEmpty()
        MangasPage(mangas, hasNext)
    }

    // =============================== Search ===============================
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query.isNotBlank()) {
            val url = "$baseUrl/comics".toHttpUrl().newBuilder()
                .addQueryParameter("src", query)
                .addQueryParameter("format", "json")
                .build()
            return GET(url, headers)
        }

        val url = "$baseUrl/comics".toHttpUrl().newBuilder()
        val status = filters.firstInstanceOrNull<StatusFilter>()?.toUriPart()
        val genre = filters.firstInstanceOrNull<GenreFilter>()?.toUriPart()

        if (!status.isNullOrEmpty()) url.addQueryParameter("status", status)
        if (!genre.isNullOrEmpty()) url.addQueryParameter("gen", genre)

        return GET(url.build(), headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = response.use {
        if (it.request.url.queryParameter("format") == "json") {
            val mangas = it.parseAs<SearchResponse>().toSMangaList(baseUrl)
            MangasPage(mangas, false)
        } else {
            val doc = it.asJsoup()
            val mangas = doc.select(".webtoon-grid a.webtoon-card").mapNotNull { it.toSManga() }
            MangasPage(mangas, false)
        }
    }

    // =========================== Manga Details ============================
    override fun mangaDetailsParse(response: Response): SManga = response.use {
        val doc = it.asJsoup()
        SManga.create().apply {
            title = doc.select(".project-title").text().takeIf { it.isNotEmpty() }
                ?: throw Exception("Título não encontrado.")
            thumbnail_url = doc.select(".project-cover").attr("abs:src")
            author = doc.select(".quick-info-item:has(.fa-pen-nib) strong").text()
            artist = doc.select(".quick-info-item:has(.fa-palette) strong").text()
            description = doc.select(".project-description").text()
            genre = doc.select(".project-genres .genre-tag").joinToString { it.text() }

            val statusText = doc.select(".status-pill").text().lowercase()
            status = when {
                statusText.contains("lançamento") -> SManga.ONGOING
                statusText.contains("completo") -> SManga.COMPLETED
                else -> SManga.UNKNOWN
            }
        }
    }

    // ============================== Chapters ==============================
    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        val doc = it.asJsoup()
        val mangaUrl = it.request.url.encodedPath

        doc.select("#tab-capitulos-list .normal_ep").mapNotNull { el ->
            val num = el.select(".num").text().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val chapterUrl = el.selectFirst("a[href]")?.attr("abs:href")?.takeIf { it.isNotEmpty() }
                ?: "$mangaUrl/ler/capitulo-$num"
            val title = el.select(".cell-title strong.line-3").text()

            SChapter.create().apply {
                setUrlWithoutDomain(chapterUrl)
                name = buildString {
                    append("Capítulo $num")
                    if (title.isNotEmpty()) {
                        append(" - ")
                        append(title)
                    }
                }
                chapter_number = num.toFloatOrNull() ?: -1f
                date_upload = dateFormat.tryParse(el.select(".cell-num .text-muted").text())
            }
        }.sortedByDescending { it.chapter_number }
    }

    // =============================== Pages ================================
    override fun pageListRequest(chapter: SChapter): Request {
        ensureLoggedIn()
        val chapterUrl = chapter.url.toAbsoluteUrl()
        return GET(chapterUrl, chapterPageHeaders(chapterUrl))
    }

    override fun pageListParse(response: Response): List<Page> = response.use {
        val html = it.body.string()

        val scriptMatch = pagesRegex.find(html)
        if (scriptMatch == null) {
            if (html.contains("showLoginModal()")) {
                throw Exception("Faça login nas configurações da extensão para ler os capítulos.")
            }
            return@use emptyList()
        }

        val jsonString = scriptMatch.groupValues[1]
        val urls = jsonString.parseAs<List<String>>()
        val pageUrl = it.request.url.toString()

        urls.mapIndexed { i, url ->
            Page(i, pageUrl, imageUrl = url)
        }
    }

    override fun imageRequest(page: Page): Request = GET(imageUrlWithRetryData(page), imageHeaders())

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // ============================== Filters ===============================
    override fun getFilterList() = FilterList(
        StatusFilter(),
        GenreFilter(),
    )

    // ============================= Preferences ============================
    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val emailPref = EditTextPreference(screen.context).apply {
            key = PREF_EMAIL_KEY
            title = "Email de Login"
            summary = "Email para acessar capítulos restritos"
            dialogTitle = "Email"
            setDefaultValue("")
            setOnPreferenceChangeListener { _, _ ->
                loginState = LoginState.UNCHECKED
                true
            }
        }
        val passPref = EditTextPreference(screen.context).apply {
            key = PREF_PASSWORD_KEY
            title = "Senha"
            summary = "Senha da sua conta"
            dialogTitle = "Senha"
            setDefaultValue("")
            setOnPreferenceChangeListener { _, _ ->
                loginState = LoginState.UNCHECKED
                true
            }
        }
        screen.addPreference(emailPref)
        screen.addPreference(passPref)
    }

    // ============================== Utilities =============================
    private fun imageRetryInterceptor(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!request.url.encodedPath.startsWith(IMAGE_DELIVERY_PREFIX) || response.code !in IMAGE_REFRESH_CODES) {
            return response
        }

        val retryData = request.url.fragment?.split('|', limit = 2) ?: return response
        if (retryData.size != 2) return response

        val pageIndex = retryData[0].toIntOrNull() ?: return response
        val chapterUrl = retryData[1].takeIf { it.startsWith("$baseUrl/") } ?: return response

        response.close()

        ensureLoggedIn()
        val freshPages = client.newCall(GET(chapterUrl, chapterPageHeaders(chapterUrl))).execute().let { pageListParse(it) }
        val freshUrl = freshPages.getOrNull(pageIndex)?.imageUrl
            ?: throw IOException("Página não encontrada após atualizar URLs assinadas.")

        return chain.proceed(
            request.newBuilder()
                .url(freshUrl)
                .build(),
        )
    }

    private fun ensureLoggedIn() {
        val email = preferences.getString(PREF_EMAIL_KEY, "") ?: ""
        val password = preferences.getString(PREF_PASSWORD_KEY, "") ?: ""

        if (email.isBlank() || password.isBlank()) {
            throw Exception("Por favor, insira suas credenciais (Email e Senha) nas configurações da extensão para ler os capítulos.")
        }

        if (loginState == LoginState.LOGGED_IN) return

        synchronized(this) {
            if (loginState == LoginState.LOGGED_IN) return

            val initDoc = client.newCall(GET(baseUrl, headers)).execute().use { it.asJsoup() }
            val csrfToken = initDoc.select("meta[name=csrf-token]").attr("content")

            if (csrfToken.isEmpty()) {
                throw Exception("Não foi possível encontrar o token de sessão CSRF.")
            }

            val formBody = FormBody.Builder()
                .add("_token", csrfToken)
                .add("USE_EMAIL", email)
                .add("password", password)
                .build()

            val loginHeaders = headersBuilder()
                .add("X-CSRF-TOKEN", csrfToken)
                .add("X-Requested-With", "XMLHttpRequest")
                .add("Origin", baseUrl)
                .add("Referer", "$baseUrl/")
                .build()

            client.newCall(POST("$baseUrl/entrar", loginHeaders, formBody)).execute().use { loginRes ->
                val login = runCatching { loginRes.parseAs<LoginResponse>() }.getOrNull()

                if (loginRes.isSuccessful && login?.isSuccess() == true) {
                    loginState = LoginState.LOGGED_IN
                } else {
                    loginState = LoginState.FAILED
                    throw Exception("Falha no login. Verifique suas credenciais nas configurações.")
                }
            }
        }
    }

    private fun imageUrlWithRetryData(page: Page) = checkNotNull(page.imageUrl).toHttpUrl().newBuilder()
        .fragment("${page.index}|${page.url}")
        .build()

    private fun Element.toSManga(): SManga? {
        val mangaUrl = attr("abs:href").takeIf { it.isNotEmpty() } ?: return null
        val mangaTitle = select(".card-title span").text().takeIf { it.isNotEmpty() } ?: return null

        return SManga.create().apply {
            setUrlWithoutDomain(mangaUrl)
            title = mangaTitle
            thumbnail_url = select(".card-thumb img").attr("abs:src")
        }
    }

    private fun String.toAbsoluteUrl(): String = if (startsWith("http")) this else baseUrl + this

    private fun chapterPageHeaders(chapterUrl: String) = headersBuilder()
        .set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
        .set("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
        .set("Referer", chapterUrl.substringBefore("/ler/", "$baseUrl/"))
        .set("Sec-Fetch-Dest", "document")
        .set("Sec-Fetch-Mode", "navigate")
        .set("Sec-Fetch-Site", "same-origin")
        .set("Sec-Fetch-User", "?1")
        .build()

    private fun imageHeaders() = super.headersBuilder()
        .set("Accept", "image/*")
        .set("Pragma", "no-cache")
        .set("Cache-Control", "no-cache")
        .set("Sec-Fetch-Dest", "empty")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    private enum class LoginState { UNCHECKED, LOGGED_IN, FAILED }

    companion object {
        private const val PREF_EMAIL_KEY = "pref_email"
        private const val PREF_PASSWORD_KEY = "pref_password"
        private const val AGE_GATE_TTL_MS = 7 * 24 * 60 * 60 * 1000L
        private const val IMAGE_DELIVERY_PREFIX = "/image/delivery/"

        private val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.ROOT)
        private val pagesRegex = Regex("""S\s*=\s*(\[.*?\])""")
        private val IMAGE_REFRESH_CODES = setOf(401, 403, 410, 419)

        private fun ageGateConsentValue(): String {
            val consentAt = System.currentTimeMillis()
            val expiresAt = consentAt + AGE_GATE_TTL_MS
            return "%7B%22consentAt%22%3A$consentAt%2C%22expiresAt%22%3A$expiresAt%7D"
        }
    }
}
