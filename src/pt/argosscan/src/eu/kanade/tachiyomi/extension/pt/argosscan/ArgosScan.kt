package eu.kanade.tachiyomi.extension.pt.argosscan

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.parseAs
import okhttp3.Dispatcher
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ArgosScan : HttpSource() {

    override val name = "Argos Scan"

    override val baseUrl = "https://argoscomics.online"
    private val apiUrl = "https://api.argoscomics.online"

    override val lang = "pt-BR"

    override val supportsLatest = true

    private val dispatcher = Dispatcher().apply {
        maxRequests = 2
        maxRequestsPerHost = 1
    }

    private val dateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()

        if (request.url.host.startsWith("api.")) {
            val cookies = network.client.cookieJar.loadForRequest(request.url)
            val hasAuth = cookies.any { it.name == "session" && it.value.isNotEmpty() }

            if (!hasAuth) {
                throw IOException("Login necessário. Abra o WebView e faça login com o Discord para usar a extensão.")
            }
        }

        val response = chain.proceed(request)

        if (response.code == 401 || response.code == 403) {
            response.close()
            throw IOException("Sessão expirada. Faça login novamente no WebView.")
        }

        response
    }

    override val client: OkHttpClient = network.client.newBuilder()
        .dispatcher(dispatcher)
        .addInterceptor(ThumbnailInterceptor())
        .addInterceptor(authInterceptor)
        .build()

    // ============================== Popular ===============================

    override fun popularMangaRequest(page: Int): Request = GET("$apiUrl/projects", headers)

    override fun popularMangaParse(response: Response): MangasPage {
        val mangas = response.parseAs<ProjectResponseDto>().toSMangaList()
        return MangasPage(mangas, false)
    }

    // =============================== Latest ===============================

    override fun latestUpdatesRequest(page: Int): Request = popularMangaRequest(page)

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    // =============================== Search ===============================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$apiUrl/projects".toHttpUrl().newBuilder()
            .fragment(query.trim())
            .build()

        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val query = response.request.url.fragment ?: ""
        val mangas = response.parseAs<ProjectResponseDto>().toSMangaList(query)
        return MangasPage(mangas, false)
    }

    // =========================== Manga Details ============================

    override fun getMangaUrl(manga: SManga): String = "$baseUrl/manga/${manga.argosSlug()}"

    override fun mangaDetailsRequest(manga: SManga): Request {
        val slug = manga.argosSlug()
        return GET("$apiUrl/projects/slug/$slug", headers)
    }

    override fun mangaDetailsParse(response: Response): SManga = response.parseAs<ProjectDto>().toSManga()

    // ============================== Chapters ==============================

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        if (manga.argosProjectId() != null) {
            return super.fetchChapterList(manga)
        }

        return Observable.fromCallable {
            val projectDto = client.newCall(mangaDetailsRequest(manga)).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Falha ao buscar os detalhes do projeto.")
                }

                response.parseAs<ProjectDto>()
            }

            client.newCall(chapterListRequest(projectDto.id())).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Falha ao buscar os capítulos.")
                }

                response.parseAs<ChapterResponseDto>().toSChapterList(projectDto.id(), dateFormat)
            }
        }
    }

    override fun chapterListRequest(manga: SManga): Request {
        val projectId = manga.argosProjectId() ?: throw IOException("ID do projeto não encontrado.")
        return chapterListRequest(projectId)
    }

    private fun chapterListRequest(projectId: String): Request {
        val url = "$apiUrl/chapters".toHttpUrl().newBuilder()
            .addQueryParameter("kind", "published")
            .addQueryParameter("project_id", projectId)
            .build()

        return GET(url, headers)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val projectId = response.request.url.queryParameter("project_id") ?: throw IOException("ID do projeto não encontrado.")
        return response.parseAs<ChapterResponseDto>().toSChapterList(projectId, dateFormat)
    }

    // =============================== Pages ================================

    override fun pageListRequest(chapter: SChapter): Request {
        val (chapterId, projectId) = chapter.argosIds()

        val url = "$apiUrl/chapters".toHttpUrl().newBuilder()
            .addQueryParameter("kind", "published")
            .addQueryParameter("project_id", projectId)
            .fragment(chapterId)
            .build()

        return GET(url, headers)
    }

    override fun pageListParse(response: Response): List<Page> {
        val chapterId = response.request.url.fragment ?: throw IOException("ID do capítulo não encontrado.")
        return response.parseAs<ChapterResponseDto>().getImagesForChapter(chapterId, apiUrl)
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException("Not used.")

    private fun SManga.argosSlug() = url.substringAfterLast("/").substringBefore(PROJECT_ID_SEPARATOR)

    private fun SManga.argosProjectId() = url.substringAfter(PROJECT_ID_SEPARATOR, "").takeIf { it.isNotEmpty() }

    private fun SChapter.argosIds(): Pair<String, String> {
        val parts = url.split(CHAPTER_URL_SEPARATOR, limit = 2)
        if (parts.size != 2 || parts.any { it.isEmpty() }) {
            throw IOException("URL do capítulo inválida.")
        }
        return parts[0] to parts[1]
    }

    companion object {
        private const val PROJECT_ID_SEPARATOR = "#"
        private const val CHAPTER_URL_SEPARATOR = "|"
    }
}
