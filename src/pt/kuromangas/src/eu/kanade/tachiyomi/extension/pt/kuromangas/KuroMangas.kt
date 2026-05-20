package eu.kanade.tachiyomi.extension.pt.kuromangas

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.JSON_MEDIA_TYPE
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.getPreferencesLazy
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonString
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class KuroMangas : HttpSource() {

    override val name = "KuroMangas"
    override val baseUrl = "https://kuromangas.com"
    override val lang = "pt-BR"
    override val supportsLatest = true

    private val apiUrl = "$baseUrl/api"
    private val cdnUrl = "https://cdn.kuromangas.com"

    private val preferences by getPreferencesLazy()
    private val webViewInterceptor = KuroWebViewInterceptor()

    override val client by lazy {
        val cdnHost = cdnUrl.toHttpUrl().host
        network.client.newBuilder()
            .rateLimit(2)
            .addInterceptor { chain ->
                val request = chain.request()
                if (request.url.host == cdnHost) {
                    return@addInterceptor chain.proceed(request)
                }

                val token = getToken()
                val authenticatedRequest = request.withAuth(token)
                val response = chain.proceed(authenticatedRequest)

                if (response.code != 401) return@addInterceptor response

                response.close()
                clearToken()
                requireKuroLogin()
            }
            .addInterceptor(
                KuroWebViewDecrypt(
                    baseUrl = baseUrl,
                    tokenProvider = { getToken() },
                    webViewInterceptor = webViewInterceptor,
                ),
            )
            .build()
    }

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("Accept", "application/json")
        .add("Referer", baseUrl)

    private val dateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private fun Request.withAuth(token: String): Request = if (token.isNotBlank()) {
        newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    } else {
        this
    }

    private fun getToken(): String {
        val savedToken = preferences.getString(PREF_ACCESS_TOKEN, "")?.trim().orEmpty()
        if (savedToken.isNotEmpty()) {
            return savedToken
        }

        val token = webViewInterceptor.getLocalStorageToken(baseUrl).orEmpty()
        if (token.isNotEmpty()) {
            preferences.edit().putString(PREF_ACCESS_TOKEN, token).apply()
        }
        return token
    }

    private fun clearToken() {
        preferences.edit().remove(PREF_ACCESS_TOKEN).apply()
    }

    private fun buildMangaListUrl(page: Int, sort: String, order: String = "DESC") = "$apiUrl/mangas".toHttpUrl().newBuilder()
        .addQueryParameter("page", page.toString())
        .addQueryParameter("limit", PAGE_LIMIT.toString())
        .addQueryParameter("sort", sort)
        .addQueryParameter("order", order)
        .build()

    private fun parseMangaList(response: Response): MangasPage = response.parseAs<MangaListResponse>().toMangasPage(cdnUrl)

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
            url.addQueryParameter("sort", "created_at")
            url.addQueryParameter("order", "DESC")
        }

        return GET(url.build(), headers)
    }

    override fun searchMangaParse(response: Response) = parseMangaList(response)

    override fun mangaDetailsRequest(manga: SManga) = GET("$apiUrl/mangas/${manga.url.substringAfterLast("/")}", headers)

    override fun mangaDetailsParse(response: Response) = response.parseAs<MangaDetailsResponse>().toSManga(cdnUrl)

    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)

    override fun chapterListParse(response: Response): List<SChapter> = response.parseAs<MangaDetailsResponse>().toChapterList(dateFormat)

    override fun pageListRequest(chapter: SChapter): Request {
        if (getToken().isEmpty()) requireKuroLogin()

        val (mangaId, chapterId) = chapter.kuroIds()
        val body = ChapterReadRequest(FIRST_PAGE).toJsonString().toRequestBody(JSON_MEDIA_TYPE)
        return POST("$apiUrl/chapters/$chapterId/read?manga_id=$mangaId", headers, body)
    }

    override fun pageListParse(response: Response): List<Page> = response.parseAs<ChapterPagesResponse>().toPages(cdnUrl)

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException()

    override fun imageRequest(page: Page): Request {
        val imageUrl = page.imageUrl ?: throw IOException("Imagem ausente na pagina ${page.index}")
        return GET(imageUrl, headersBuilder().set("Referer", baseUrl).build())
    }

    override fun getMangaUrl(manga: SManga) = "$baseUrl/manga/${manga.url.substringAfterLast("/")}"

    override fun getChapterUrl(chapter: SChapter): String {
        // chapter.url format: /chapter/{mangaId}/{chapterId}
        val parts = chapter.url.removePrefix("/chapter/").split("/")
        val mangaId = parts.getOrNull(0) ?: ""
        val chapterId = parts.getOrNull(1) ?: ""
        return "$baseUrl/reader/$mangaId/$chapterId"
    }

    // ============================= Auth ===================================

    private fun login(email: String, password: String): String {
        val payload = buildJsonObject {
            put("email", email)
            put("password", password)
        }.toString()
        val requestBody = payload.toRequestBody(JSON_MEDIA_TYPE)
        val request = POST("$apiUrl/auth/login", headers, requestBody)
        val response = network.client.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            throw Exception("Login failed: ${response.code}")
        }
        return response.parseAs<LoginResponse>().token
    }

    // ============================= Preferences ============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val warning = "⚠️ Os dados inseridos nesta seção serão usados somente para realizar o login na fonte"
        val message = "Insira %s para prosseguir com o acesso aos recursos disponíveis na fonte"

        EditTextPreference(screen.context).apply {
            key = PREF_EMAIL
            title = "📧 Email"
            summary = "Email de acesso"
            dialogMessage = buildString {
                appendLine(message.format("seu email"))
                append("\n$warning")
            }
            setDefaultValue("")
        }.let(screen::addPreference)

        EditTextPreference(screen.context).apply {
            key = PREF_PASSWORD
            title = "🔑 Senha"
            summary = "Senha de acesso"
            dialogMessage = buildString {
                appendLine(message.format("sua senha"))
                append("\n$warning")
            }
            setDefaultValue("")
        }.let(screen::addPreference)
    }

    override fun getFilterList() = getFilters()

    private fun SChapter.kuroIds(): Pair<String, String> {
        val parts = url.removePrefix("/chapter/").split("/")
        return parts[0] to parts[1]
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val PAGE_LIMIT = 24
        private const val PREF_ACCESS_TOKEN = "pref_access_token"
    }
}
