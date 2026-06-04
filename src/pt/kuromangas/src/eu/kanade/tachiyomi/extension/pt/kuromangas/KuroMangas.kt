package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.getPreferencesLazy
import keiyoushi.utils.parseAs
import kotlinx.serialization.json.JsonObject
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class KuroMangas :
    HttpSource(),
    ConfigurableSource {

    override val name = "KuroMangas"
    override val baseUrl = "https://kuromangas.com"
    override val lang = "pt-BR"
    override val supportsLatest = true

    private val apiUrl = "$baseUrl/api"
    private val cdnUrl = "https://cdn.kuromangas.com"

    private val preferences by getPreferencesLazy()

    private val apiDecryptInterceptor = ApiDecryptInterceptor()

    private val loginClient by lazy {
        network.cloudflareClient.newBuilder()
            .addInterceptor(apiDecryptInterceptor)
            .build()
    }

    private val tokenProvider by lazy {
        AuthTokenProvider(
            preferences = preferences,
            client = loginClient,
            apiUrl = apiUrl,
            loginHeaders = loginHeaders().build(),
        )
    }

    override val client by lazy {
        network.cloudflareClient.newBuilder()
            .rateLimit(2)
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(apiDecryptInterceptor)
            .build()
    }

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("Accept", "application/json")
        .add("Referer", baseUrl)

    private fun loginHeaders() = headersBuilder()
        .set("Content-Type", "application/json")
        .set("Referer", "$baseUrl/login")

    private val dateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private fun buildMangaListUrl(page: Int, sort: String, order: String = "DESC") = "$apiUrl/mangas".toHttpUrl().newBuilder()
        .addQueryParameter("page", page.toString())
        .addQueryParameter("limit", PAGE_LIMIT.toString())
        .addQueryParameter("sort", sort)
        .addQueryParameter("order", order)
        .build()

    private fun parseMangaList(response: Response): MangasPage = response.use {
        it.parseAs<MangaListResponse>().toMangasPage(cdnUrl)
    }

    override fun popularMangaRequest(page: Int) = GET(buildMangaListUrl(page, "view_count"), headers)
    override fun popularMangaParse(response: Response) = parseMangaList(response)

    override fun latestUpdatesRequest(page: Int) = GET(buildMangaListUrl(page, "created_at"), headers)
    override fun latestUpdatesParse(response: Response) = parseMangaList(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", PAGE_LIMIT.toString())

        if (query.isNotBlank()) url.addQueryParameter("search", query)

        filters.firstInstanceOrNull<SortFilter>()?.let {
            url.addQueryParameter("sort", it.selectedSort)
            url.addQueryParameter("order", it.selectedOrder)
        } ?: run {
            url.addQueryParameter("sort", "view_count")
            url.addQueryParameter("order", "DESC")
        }

        return GET(url.build(), headers)
    }

    override fun searchMangaParse(response: Response) = parseMangaList(response)

    override fun mangaDetailsRequest(manga: SManga) = GET("$apiUrl/mangas/${manga.url.substringAfterLast("/")}", headers)

    override fun mangaDetailsParse(response: Response) = response.use {
        it.parseAs<MangaDetailsResponse>().toSManga(cdnUrl)
    }

    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)

    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        it.parseAs<MangaDetailsResponse>().toChapterList(dateFormat)
    }

    override fun pageListRequest(chapter: SChapter): Request {
        tokenProvider.requireToken()

        val chapterId = chapter.kuroIds().second
        return GET("$apiUrl/chapters/$chapterId", headers)
    }

    override fun pageListParse(response: Response): List<Page> = response.use {
        it.parseAs<JsonObject>().toPages(cdnUrl)
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException()

    override fun imageRequest(page: Page): Request {
        val imageUrl = page.imageUrl ?: throw IOException("Imagem ausente na pagina ${page.index}")
        return GET(imageUrl, headersBuilder().set("Referer", baseUrl).build())
    }

    override fun getMangaUrl(manga: SManga) = "$baseUrl/manga/${manga.url.substringAfterLast("/")}"

    override fun getChapterUrl(chapter: SChapter): String {
        val (mangaId, chapterId) = chapter.kuroIds()
        return "$baseUrl/read/$mangaId/$chapterId"
    }

    override fun getFilterList() = getFilters()

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val warning = "Os dados inseridos nesta secao serao usados somente para realizar o login na fonte."
        val message = "Insira %s para prosseguir com o acesso aos recursos disponiveis na fonte."

        EditTextPreference(screen.context).apply {
            key = AuthTokenProvider.EMAIL_PREF
            title = "E-mail"
            summary = "E-mail de acesso"
            dialogMessage = buildString {
                appendLine(message.format("seu e-mail"))
                append("\n$warning")
            }
            setDefaultValue("")
            setOnPreferenceChangeListener { _, newValue ->
                tokenProvider.clear()
                val password = preferences.getString(AuthTokenProvider.PASSWORD_PREF, "").orEmpty()
                tokenProvider.checkLogin(newValue as String, password)
                true
            }
        }.let(screen::addPreference)

        EditTextPreference(screen.context).apply {
            key = AuthTokenProvider.PASSWORD_PREF
            title = "Senha"
            summary = "Senha de acesso"
            dialogMessage = buildString {
                appendLine(message.format("sua senha"))
                append("\n$warning")
            }
            setDefaultValue("")
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            setOnPreferenceChangeListener { _, newValue ->
                tokenProvider.clear()
                val email = preferences.getString(AuthTokenProvider.EMAIL_PREF, "").orEmpty()
                tokenProvider.checkLogin(email, newValue as String)
                true
            }
        }.let(screen::addPreference)
    }

    private fun SChapter.kuroIds(): Pair<String, String> {
        val parts = url.removePrefix("/chapter/").split("/")
        return parts[0] to parts[1]
    }

    companion object {
        private const val PAGE_LIMIT = 24
    }
}
