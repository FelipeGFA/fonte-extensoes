package eu.kanade.tachiyomi.extension.pt.rfdragonscan

import android.content.SharedPreferences
import android.text.InputType
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
import keiyoushi.utils.extractNextJs
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.getPreferencesLazy
import keiyoushi.utils.toJsonRequestBody
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RFDragonScan :
    HttpSource(),
    ConfigurableSource {

    override val name: String = "RF Dragon Scan"

    override val baseUrl: String = "https://rfdragonscan.net"

    override val lang: String = "pt-BR"

    override val supportsLatest: Boolean = true

    override val versionId: Int = 2

    private val preferences: SharedPreferences by getPreferencesLazy()

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .set("Referer", "$baseUrl/")

    override fun getMangaUrl(manga: SManga): String = "$baseUrl${manga.url}"

    override fun getChapterUrl(chapter: SChapter): String = chapter.publicUrl()

    // ============================== Popular ===============================

    override fun popularMangaRequest(page: Int): Request {
        val headers = actionHeaders(POPULAR_TOKEN, "$baseUrl/")
        val payload = listOf("all_time").toJsonRequestBody()
        return POST(baseUrl, headers, payload)
    }

    override fun popularMangaParse(response: Response): MangasPage = response.use {
        val mangas = it.extractNextJs<List<MangaDto>>()
            .orEmpty()
            .mapNotNull(MangaDto::toSManga)
        MangasPage(mangas, false)
    }

    // =============================== Latest ===============================

    override fun latestUpdatesRequest(page: Int): Request = GET(baseUrl, headers)

    override fun latestUpdatesParse(response: Response): MangasPage = response.use {
        requireNotNull(it.extractNextJs<LatestDto> { element -> element.isLatestPayload() }) {
            "Nao foi possivel extrair os ultimos lancamentos"
        }.toMangasPage()
    }

    // =============================== Search ===============================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query.isBlank()) {
            val selectedFilter = filters.firstInstanceOrNull<GenreFilter>()?.selectedFilter()
                ?: filters.firstInstanceOrNull<StatusFilter>()?.selectedFilter()
                ?: filters.firstInstanceOrNull<TypeFilter>()?.selectedFilter()
            val url = "$baseUrl$PROJECTS_PATH".toHttpUrl().newBuilder().apply {
                addQueryParameter("page", page.toString())
                selectedFilter?.let {
                    addQueryParameter("filter", it.filter)
                    addQueryParameter("term", it.term)
                }
            }.build()

            return GET(url, headers)
        }

        val headers = actionHeaders(SEARCH_TOKEN, "$baseUrl/")
        val payload = listOf(query).toJsonRequestBody()
        return POST(baseUrl, headers, payload)
    }

    override fun searchMangaParse(response: Response): MangasPage = response.use {
        if (it.request.url.encodedPath == PROJECTS_PATH) {
            return@use requireNotNull(it.extractNextJs<ProjectsPageDto> { element -> element.isProjectsPagePayload() }) {
                "Nao foi possivel extrair a lista de projetos"
            }.toMangasPage()
        }

        val mangas = it.extractNextJs<List<MangaDto>>()
            .orEmpty()
            .mapNotNull(MangaDto::toSManga)
        MangasPage(mangas, false)
    }

    override fun getFilterList(): FilterList = getFilters()

    // ============================ Manga Details ============================

    override fun mangaDetailsRequest(manga: SManga): Request {
        val url = getMangaUrl(manga)
        val payload = url.toHttpUrl().pathSegments.toJsonRequestBody()
        val headers = actionHeaders(DETAILS_TOKEN, url)
        return POST(url, headers, payload)
    }

    override fun mangaDetailsParse(response: Response): SManga = response.use {
        requireNotNull(it.extractNextJs<MangaDetailsDto> { element -> element.isMangaDetailsPayload() }) {
            "Nao foi possivel extrair os detalhes da obra"
        }.toSManga()
    }

    // ============================== Chapters ==============================

    override fun chapterListRequest(manga: SManga): Request {
        val url = getMangaUrl(manga)
        val payload = url.toHttpUrl().pathSegments.toJsonRequestBody()
        val headers = actionHeaders(CHAPTERS_TOKEN, url)
        return POST(url, headers, payload)
    }

    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        val mangaPath = it.request.url.encodedPath
        it.extractNextJs<ChapterGroupsDto> { element -> element.isChapterGroupsPayload() }
            ?.toSChapterList(mangaPath)
            .orEmpty()
    }

    // ================================ Pages ================================

    override fun pageListRequest(chapter: SChapter): Request {
        val chapterUrl = "$baseUrl${chapter.url}".toHttpUrl()
        if (chapterUrl.queryParameter(CHAPTER_ACCESS_TYPE_QUERY) == COINS_ACCESS_TYPE) {
            throw IOException(COINS_CHAPTER_ERROR)
        }

        ensureLoggedIn()

        val url = chapter.publicUrl()
        val segments = chapterUrl.pathSegments
        val payload = listOf(segments.first(), segments.last()).toJsonRequestBody()
        val headers = actionHeaders(PAGES_TOKEN, url)
        return POST(url, headers, payload)
    }

    override fun pageListParse(response: Response): List<Page> = response.use {
        it.extractNextJs<PagesDto> { element -> element.isPagesPayload() }
            ?.toPageList(it.request.url.toString())
            .orEmpty()
    }

    override fun imageRequest(page: Page): Request {
        val imageHeaders = headersBuilder()
            .set("Referer", page.url)
            .build()

        return GET(requireNotNull(page.imageUrl), imageHeaders)
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // ============================= Preferences =============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        EditTextPreference(screen.context).apply {
            key = PREF_EMAIL
            title = "Email"
            summary = "Necessario apenas para carregar paginas"
            setDefaultValue("")
        }.let(screen::addPreference)

        EditTextPreference(screen.context).apply {
            key = PREF_PASSWORD
            title = "Senha"
            summary = "Necessaria apenas para carregar paginas"
            setDefaultValue("")
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }.let(screen::addPreference)
    }

    // ============================= Authentication ==========================

    @Synchronized
    private fun ensureLoggedIn() {
        val url = baseUrl.toHttpUrl()
        val cachedToken = client.cookieJar.loadForRequest(url)
            .firstOrNull { it.name == ACCESS_TOKEN_COOKIE }
            ?.value

        if (!cachedToken.isNullOrEmpty()) return

        val email = preferences.getString(PREF_EMAIL, "").orEmpty()
        val password = preferences.getString(PREF_PASSWORD, "").orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            throw IOException("Login necessario para carregar paginas. Configure email e senha nas preferencias da extensao.")
        }

        val response = client.newCall(loginRequest(email, password)).execute()
        response.use {
            if (!it.isSuccessful) {
                throw IOException("Falha no login: HTTP ${it.code}")
            }

            val hasAccessToken = it.headers("Set-Cookie")
                .any { cookie -> cookie.startsWith("$ACCESS_TOKEN_COOKIE=") }
            if (!hasAccessToken) {
                throw IOException("Falha no login: cookie access_token nao recebido")
            }
        }
    }

    private fun loginRequest(email: String, password: String): Request {
        val url = "$baseUrl/login"
        val headers = actionHeaders(LOGIN_TOKEN, url)
        val payload = listOf(email, password).toJsonRequestBody()
        return POST(url, headers, payload)
    }

    private fun actionHeaders(action: String, referer: String): Headers = headersBuilder()
        .set("Accept", "text/x-component")
        .set("Referer", referer)
        .set("Origin", baseUrl)
        .set("Next-Action", action)
        .build()

    private fun SChapter.publicUrl(): String {
        val url = "$baseUrl${this.url}".toHttpUrl()
        return "$baseUrl${url.encodedPath}"
    }

    companion object {
        private const val POPULAR_TOKEN = "40ec119c02c83bcccc135a8aacb1097a8d87cb2879"
        private const val SEARCH_TOKEN = "406369e6483a4fe640a38cebf46ca5ea2385392f8d"
        private const val DETAILS_TOKEN = "60bd903bddc3d9d07f2b58fe32f0238afd74e492d6"
        private const val CHAPTERS_TOKEN = "6075c7373783e0d2488372dc7fcb9ffe1470bc41d2"
        private const val PAGES_TOKEN = "605aecabcce97cec193f09ebe5fe3a9ae46e432ea2"
        private const val LOGIN_TOKEN = "600165150b15a3870c9e076c863daec8d24748e458"
        private const val ACCESS_TOKEN_COOKIE = "access_token"
        private const val PREF_EMAIL = "email"
        private const val PREF_PASSWORD = "password"
        private const val PROJECTS_PATH = "/projetos"
    }
}
