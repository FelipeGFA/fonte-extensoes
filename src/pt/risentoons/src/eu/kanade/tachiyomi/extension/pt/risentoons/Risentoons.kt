package eu.kanade.tachiyomi.extension.pt.risentoons

import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.lib.cookieinterceptor.CookieInterceptor
import keiyoushi.network.rateLimit
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.getPreferencesLazy
import keiyoushi.utils.parseAs
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class Risentoons :
    HttpSource(),
    ConfigurableSource {

    override val name = "Risentoons"

    override val baseUrl = "https://risentoons.xyz"

    override val lang = "pt-BR"

    override val supportsLatest = true

    private val apiUrl = "$baseUrl/api"

    private val preferences by getPreferencesLazy()

    private val authProvider by lazy {
        AuthSessionProvider(
            preferences = preferences,
            client = network.client,
            apiUrl = apiUrl,
            loginHeaders = loginHeaders,
        )
    }

    override val client: OkHttpClient by lazy {
        val session = authProvider.requireSession()
        network.client.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                if (request.url.host != API_HOST) {
                    return@addInterceptor chain.proceed(request)
                }

                chain.proceed(request.withAuth(session))
            }
            .addNetworkInterceptor(CookieInterceptor(API_HOST, session.toCookies()))
            .rateLimit(2)
            .build()
    }

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .set("Accept", "application/json, text/plain, */*")
        .set("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
        .set("X-Rip-Client", RIP_CLIENT)
        .set("Origin", baseUrl)
        .set("Referer", "$baseUrl/")

    private val loginHeaders: Headers
        get() = headersBuilder()
            .set("Referer", "$baseUrl/login")
            .build()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // ============================= Popular ================================

    override fun popularMangaRequest(page: Int): Request {
        authProvider.requireSession()
        val url = "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addQueryParameter("limit", PAGE_LIMIT.toString())
            .addQueryParameter("sort", "ranking_score")
            .build()
        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val result = response.parseAs<MangaListResponse>()
        return result.toMangasPage(hasNextPage = false)
    }

    // ============================= Latest =================================

    override fun latestUpdatesRequest(page: Int): Request {
        authProvider.requireSession()
        val url = "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addQueryParameter("limit", PAGE_LIMIT.toString())
            .addQueryParameter("sort", "-last_chapter_at")
            .build()
        return GET(url, headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        val result = response.parseAs<MangaListResponse>()
        return result.toMangasPage(hasNextPage = false)
    }

    // ============================= Search =================================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        authProvider.requireSession()
        val url = "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addQueryParameter("limit", PAGE_LIMIT.toString())
            .addQueryParameter("page", page.toString())

        if (query.isNotBlank()) {
            url.addQueryParameter("search", query)
        }

        filters.firstInstanceOrNull<TypeFilter>()?.selected?.let {
            url.addQueryParameter("type_filter", it)
        }

        filters.firstInstanceOrNull<StatusFilter>()?.selected?.let {
            url.addQueryParameter("status", it)
        }

        filters.firstInstanceOrNull<GenreFilter>()?.selected?.let {
            url.addQueryParameter("genres", it)
        }

        return GET(url.build(), headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val result = response.parseAs<MangaListResponse>()
        return result.toMangasPage()
    }

    // ============================= Details ================================

    override fun mangaDetailsRequest(manga: SManga): Request {
        authProvider.requireSession()
        return GET("$apiUrl/mangas/${manga.risentoonsSlug()}", headers)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val result = response.parseAs<DataResponse<MangaDto>>()
        return result.value().toSManga(initialized = true)
    }

    // ============================= Chapters ===============================

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> = Observable.fromCallable {
        authProvider.requireSession()
        val mangaId = manga.risentoonsId() ?: fetchMangaId(manga.risentoonsSlug())
        val url = "$apiUrl/mangas/$mangaId/chapters".toHttpUrl().newBuilder()
            .addQueryParameter("page", "1")
            .addQueryParameter("limit", CHAPTER_LIMIT.toString())
            .addQueryParameter("order", "desc")
            .build()

        client.newCall(GET(url, headers)).execute().use { response ->
            response.parseAs<ChapterListResponse>()
                .toSChapterList(manga.risentoonsSlug(), dateFormat)
        }
    }

    override fun chapterListRequest(manga: SManga): Request = throw UnsupportedOperationException()

    override fun chapterListParse(response: Response): List<SChapter> = throw UnsupportedOperationException()

    // ============================= Pages ==================================

    override fun pageListRequest(chapter: SChapter): Request {
        authProvider.requireSession()
        val chapterId = chapter.risentoonsId()
            ?: throw IOException("ID do capitulo ausente")
        return GET("$apiUrl/mangas/chapters/$chapterId/pages", headers)
    }

    override fun pageListParse(response: Response): List<Page> {
        val result = response.parseAs<PageListResponse>()
        return result.toPageList(baseUrl)
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    override fun imageRequest(page: Page): Request {
        val imageHeaders = headersBuilder()
            .set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .set("Referer", "$baseUrl/")
            .build()
        return GET(page.imageUrl!!, imageHeaders)
    }

    // ============================= Filters ================================

    override fun getFilterList(): FilterList = getFilters()

    // ============================= Preferences ============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        EditTextPreference(screen.context).apply {
            key = AuthSessionProvider.EMAIL_PREF
            title = "Email"
            summary = "Email para login automatico"
            dialogMessage = LOGIN_DIALOG_MESSAGE
            setDefaultValue("")
            setOnPreferenceChangeListener { _, _ ->
                authProvider.clear()
                true
            }
        }.let(screen::addPreference)

        EditTextPreference(screen.context).apply {
            key = AuthSessionProvider.PASSWORD_PREF
            title = "Senha"
            summary = "Senha para login automatico"
            dialogMessage = LOGIN_DIALOG_MESSAGE
            setDefaultValue("")
            setOnPreferenceChangeListener { _, _ ->
                authProvider.clear()
                true
            }
        }.let(screen::addPreference)
    }

    // ============================= Utils ==================================

    override fun getMangaUrl(manga: SManga): String = "$baseUrl/biblioteca/${manga.risentoonsSlug()}"

    override fun getChapterUrl(chapter: SChapter): String {
        val url = "$baseUrl${chapter.url}".toHttpUrl()
        return "$baseUrl/${url.pathSegments.joinToString("/")}"
    }

    private fun fetchMangaId(slug: String): String = client.newCall(GET("$apiUrl/mangas/$slug", headers)).execute().use { response ->
        response.parseAs<DataResponse<MangaDto>>().value().id()
    }

    private fun Request.withAuth(session: AuthSession): Request = newBuilder()
        .header("Authorization", "Bearer ${session.sessionId}")
        .header("Autentication", "Bearer ${session.sessionId}")
        .build()

    private fun SManga.risentoonsSlug(): String = url.risentoonsUrl().pathSegments.let { segments ->
        when {
            segments.firstOrNull() == "biblioteca" && segments.size >= 2 -> segments[1]
            segments.firstOrNull() == "manga" && segments.size >= 2 -> segments[1]
            else -> segments.lastOrNull().orEmpty()
        }
    }

    private fun SManga.risentoonsId(): String? = url.risentoonsUrl().queryParameter("id")

    private fun SChapter.risentoonsId(): String? = url.risentoonsUrl().queryParameter("id")

    private fun String.risentoonsUrl() = when {
        startsWith("http") -> this
        startsWith("/") -> "$baseUrl$this"
        else -> "$baseUrl/$this"
    }.toHttpUrl()

    companion object {
        private const val API_HOST = "risentoons.xyz"
        private const val PAGE_LIMIT = 2000
        private const val CHAPTER_LIMIT = 1000
        private const val RIP_CLIENT = "V6"
        private const val LOGIN_DIALOG_MESSAGE =
            "Insira as credenciais da sua conta Risentoons. O login e obrigatorio para acessar a API."
    }
}
