package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
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
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class KuroMangas : HttpSource(), ConfigurableSource {

    override val name = "KuroMangas"
    override val baseUrl = "https://beta.kuromangas.com"
    override val lang = "pt-BR"
    override val supportsLatest = true

    private val apiUrl = "$baseUrl/api"
    private val cdnUrl = "https://cdn.kuromangas.com"

    private val preferences by getPreferencesLazy()

    override val client by lazy {
        val token = getToken()
        val cdnHost = cdnUrl.toHttpUrl().host
        network.cloudflareClient.newBuilder()
            .rateLimit(2)
            .addInterceptor(
                KuroWebViewDecrypt(baseUrl) {
                    val email = preferences.getString(PREF_EMAIL, "") ?: ""
                    val password = preferences.getString(PREF_PASSWORD, "") ?: ""
                    email to password
                },
            )
            .apply {
                if (token.isNotEmpty()) {
                    addInterceptor { chain ->
                        val request = chain.request()
                        if (request.url.host == cdnHost) return@addInterceptor chain.proceed(request)
                        chain.proceed(request.newBuilder().header("Authorization", "Bearer $token").build())
                    }
                }
            }
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

    private fun getToken(): String {
        val email = preferences.getString(PREF_EMAIL, "") ?: ""
        val password = preferences.getString(PREF_PASSWORD, "") ?: ""
        if (email.isEmpty() || password.isEmpty()) return ""

        val token = preferences.getString(PREF_TOKEN, "") ?: ""
        if (token.isNotEmpty()) return token

        return login(email, password)
    }

    private fun login(email: String, password: String): String {
        return try {
            val result = KuroWebViewInterceptor().getDecryptedData(
                pageUrl = "$baseUrl/login",
                targetEndpoint = "auth/login",
                loginCredentials = KuroWebViewInterceptor.LoginCredentials(email, password),
            ) ?: return ""

            val loginResponse = result.data.parseAs<LoginResponse>()
            val token = loginResponse.token
            preferences.edit().putString(PREF_TOKEN, token).apply()
            token
        } catch (e: Exception) {
            ""
        }
    }

    private fun checkLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) return

        Thread {
            val token = login(email, password)
            Handler(Looper.getMainLooper()).post {
                val message = if (token.isNotEmpty()) {
                    "Login realizado com sucesso"
                } else {
                    "Falha no login - verifique suas credenciais"
                }
                Toast.makeText(Injekt.get<Application>(), message, Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun buildMangaListUrl(page: Int, sort: String, order: String = "DESC") =
        "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", PAGE_LIMIT.toString())
            .addQueryParameter("sort", sort)
            .addQueryParameter("order", order)
            .build()

    private fun parseMangaList(response: Response): MangasPage {
        val result = response.parseAs<MangaListResponse>()
        return MangasPage(result.data.map { it.toSManga(cdnUrl) }, result.pagination.hasNextPage())
    }

    // ============================= Popular ================================

    override fun popularMangaRequest(page: Int) = GET(buildMangaListUrl(page, "view_count"), headers)
    override fun popularMangaParse(response: Response) = parseMangaList(response)

    // ============================= Latest =================================

    override fun latestUpdatesRequest(page: Int) = GET(buildMangaListUrl(page, "created_at"), headers)
    override fun latestUpdatesParse(response: Response) = parseMangaList(response)

    // ============================= Search =================================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", PAGE_LIMIT.toString())

        if (query.isNotBlank()) url.addQueryParameter("search", query)

        filters.filterIsInstance<SortFilter>().firstOrNull()?.let {
            url.addQueryParameter("sort", it.selectedSort)
            url.addQueryParameter("order", it.selectedOrder)
        } ?: run {
            url.addQueryParameter("sort", "created_at")
            url.addQueryParameter("order", "DESC")
        }

        return GET(url.build(), headers)
    }

    override fun searchMangaParse(response: Response) = parseMangaList(response)

    // ============================= Details ================================

    override fun mangaDetailsRequest(manga: SManga) = GET("$apiUrl/mangas/${manga.url.substringAfterLast("/")}", headers)

    override fun mangaDetailsParse(response: Response) = response.parseAs<MangaDetailsResponse>().manga.toSManga(cdnUrl)

    // ============================= Chapters ===============================

    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)

    override fun chapterListParse(response: Response): List<SChapter> {
        val result = response.parseAs<MangaDetailsResponse>()
        return result.chapters.map { it.toSChapter(result.manga.id, dateFormat) }.sortedByDescending { it.chapter_number }
    }

    // ============================= Pages ==================================

    override fun pageListRequest(chapter: SChapter): Request {
        val email = preferences.getString(PREF_EMAIL, "") ?: ""
        val password = preferences.getString(PREF_PASSWORD, "") ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            val message = "Faça o login nas configurações da extensão"
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(Injekt.get<Application>(), message, Toast.LENGTH_LONG).show()
            }
            throw Exception(message)
        }

        val parts = chapter.url.split("/").filter { it.isNotEmpty() }
        val mangaId = parts.getOrNull(1) ?: ""
        val chapterId = parts.getOrNull(2) ?: chapter.url.substringAfterLast("/")
        val body = """{"page":1}""".toRequestBody("application/json".toMediaType())
        return POST("$apiUrl/chapters/$chapterId/read?manga_id=$mangaId", headers, body)
    }

    override fun pageListParse(response: Response): List<Page> {
        return response.parseAs<ChapterPagesResponse>().pages.mapIndexed { index, pageUrl ->
            val fixedUrl = pageUrl.replaceFirst("^/uploads/".toRegex(), "/")
            Page(index, imageUrl = if (fixedUrl.startsWith("http")) fixedUrl else "$cdnUrl$fixedUrl")
        }
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException()

    override fun imageRequest(page: Page) = GET(page.imageUrl!!, headersBuilder().set("Referer", baseUrl).build())

    // ============================= Utils ==================================

    override fun getMangaUrl(manga: SManga) = "$baseUrl/manga/${manga.url.substringAfterLast("/")}"

    override fun getChapterUrl(chapter: SChapter): String {
        val parts = chapter.url.removePrefix("/chapter/").split("/")
        return "$baseUrl/reader/${parts.getOrNull(0) ?: ""}/${parts.getOrNull(1) ?: ""}"
    }

    override fun getFilterList() = getFilters()

    // ============================= Settings ===============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val warning = "⚠️ Os dados inseridos nesta seção serão usados somente para realizar o login na fonte."
        val message = "Insira %s para prosseguir com o acesso aos recursos disponíveis na fonte."

        EditTextPreference(screen.context).apply {
            key = PREF_EMAIL
            title = "E-mail"
            summary = "E-mail de acesso"
            dialogMessage = buildString {
                appendLine(message.format("seu e-mail"))
                append("\n$warning")
            }
            setDefaultValue("")
            setOnPreferenceChangeListener { _, newValue ->
                preferences.edit().remove(PREF_TOKEN).apply()
                val password = preferences.getString(PREF_PASSWORD, "") ?: ""
                checkLogin(newValue as String, password)
                true
            }
        }.let(screen::addPreference)

        EditTextPreference(screen.context).apply {
            key = PREF_PASSWORD
            title = "Senha"
            summary = "Senha de acesso"
            dialogMessage = buildString {
                appendLine(message.format("sua senha"))
                append("\n$warning")
            }
            setDefaultValue("")
            setOnPreferenceChangeListener { _, newValue ->
                preferences.edit().remove(PREF_TOKEN).apply()
                val email = preferences.getString(PREF_EMAIL, "") ?: ""
                checkLogin(email, newValue as String)
                true
            }
        }.let(screen::addPreference)
    }

    companion object {
        private const val PAGE_LIMIT = 24
        private const val PREF_EMAIL = "pref_email"
        private const val PREF_PASSWORD = "pref_password"
        private const val PREF_TOKEN = "pref_token"
    }
}
