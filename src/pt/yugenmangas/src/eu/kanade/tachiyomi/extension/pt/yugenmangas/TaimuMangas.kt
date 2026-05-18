package eu.kanade.tachiyomi.extension.pt.yugenmangas

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.extractNextJs
import keiyoushi.utils.firstInstanceOrNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TaimuMangas : HttpSource() {

    override val name = "Taimu Mangás"

    override val baseUrl = "https://taimumangas.rzword.xyz"

    private val imageBaseUrl = "https://api.taimumangas.com/media"

    override val lang = "pt-BR"

    override val supportsLatest = true

    override val versionId = 2

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .rateLimit(2)
        .build()

    private val chapterDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // ================================ Popular =======================================

    override fun popularMangaRequest(page: Int): Request {
        val url = "$baseUrl/biblioteca".toHttpUrl().newBuilder()
            .addQueryParameter("sort_by", DEFAULT_SORT_BY)
            .addQueryParameter("sort_order", DEFAULT_SORT_ORDER)
            .addQueryParameter("page", page.toString())
            .build()

        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage = parseLibraryResponse(response, "Nao foi possivel extrair a lista popular")

    // ================================ Latest =======================================

    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$baseUrl/capitulos".toHttpUrl().newBuilder()
            .apply {
                if (page > 1) {
                    addQueryParameter("page", page.toString())
                }
            }
            .build()

        return GET(url, headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = response.use { res ->
        val payload = res.extractNextJs<LatestPageDto> { it.isLatestPagePayload() }
            ?: throw IOException("Nao foi possivel extrair a lista de atualizacoes")

        payload.toMangasPage(imageBaseUrl)
    }

    // ================================ Search =======================================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val status = filters.firstInstanceOrNull<StatusFilter>()?.selectedValue.orEmpty()
        val sort = filters.firstInstanceOrNull<SortFilter>()
        val genre = filters.firstInstanceOrNull<GenreFilter>()

        val url = "$baseUrl/biblioteca".toHttpUrl().newBuilder().apply {
            if (query.isNotBlank()) {
                addQueryParameter("name", query)
            }

            if (status.isNotEmpty()) {
                addQueryParameter("status", status)
            }

            genre?.includedIds
                ?.takeIf(String::isNotEmpty)
                ?.let { addQueryParameter("genres_include", it) }

            genre?.excludedIds
                ?.takeIf(String::isNotEmpty)
                ?.let { addQueryParameter("genres_exclude", it) }

            addQueryParameter("sort_by", sort?.selectedValue ?: DEFAULT_SORT_BY)
            addQueryParameter("sort_order", sort?.selectedOrder ?: DEFAULT_SORT_ORDER)
            addQueryParameter("page", page.toString())
        }.build()

        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = parseLibraryResponse(
        response,
        errorMessage = "Nao foi possivel extrair a lista de busca",
        emptyResultMessage = "Nenhum resultado encontrado para esta pesquisa/filtro",
    )

    // ================================ Details =======================================

    override fun mangaDetailsParse(response: Response): SManga = response.use { res ->
        val payload = res.extractNextJs<DetailsPageDto> { it.isDetailsPagePayload() }
            ?: throw IOException("Nao foi possivel extrair os detalhes da obra")

        payload.toSManga(imageBaseUrl, res.request.url.encodedPath)
    }

    // ================================ Chapters =======================================

    override fun chapterListRequest(manga: SManga): Request {
        val url = (baseUrl + manga.url).toHttpUrl().newBuilder()
            .addQueryParameter("order", CHAPTER_ORDER)
            .build()

        return GET(url, headers)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val requestUrl = response.request.url
        val firstPage = parseDetailsPage(response, "Nao foi possivel extrair os capitulos da obra")

        val chapters = buildList {
            addAll(firstPage.toSChapterList(chapterDateFormat))

            val currentPage = firstPage.currentChapterPage
            val totalPages = firstPage.totalChapterPages

            if (currentPage < totalPages) {
                for (page in (currentPage + 1)..totalPages) {
                    val nextUrl = requestUrl.newBuilder()
                        .setQueryParameter("page", page.toString())
                        .setQueryParameter("order", CHAPTER_ORDER)
                        .build()

                    val nextPage = client.newCall(GET(nextUrl, headers)).execute().let {
                        parseDetailsPage(it, "Nao foi possivel extrair a pagina $page de capitulos")
                    }

                    addAll(nextPage.toSChapterList(chapterDateFormat))
                }
            }
        }.distinctBy { it.url }

        if (chapters.isEmpty()) {
            throw IOException("Nenhum capitulo encontrado para esta obra")
        }

        return chapters
    }

    // ================================ Pages =======================================

    override fun pageListParse(response: Response): List<Page> = response.use { res ->
        val payload = res.extractNextJs<ReaderPageDto> { it.isReaderPagePayload() }
            ?: throw IOException("Nao foi possivel extrair as paginas do capitulo")

        payload.toPageList(imageBaseUrl).also { pages ->
            if (pages.isEmpty()) {
                throw IOException("Nenhuma pagina encontrada para este capitulo")
            }
        }
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // ================================ Filters =======================================

    override fun getFilterList(): FilterList = FilterList(
        StatusFilter(),
        SortFilter(),
        Filter.Separator(),
        GenreFilter(),
    )

    // ================================ Utils =======================================

    private fun parseLibraryResponse(
        response: Response,
        errorMessage: String,
        emptyResultMessage: String? = null,
    ): MangasPage = response.use { res ->
        val payload = res.extractNextJs<LibraryPageDto> { it.isLibraryPagePayload() }
            ?: throw IOException(errorMessage)

        payload.toMangasPage(imageBaseUrl).also { page ->
            if (emptyResultMessage != null && page.mangas.isEmpty()) {
                throw IOException(emptyResultMessage)
            }
        }
    }

    private fun parseDetailsPage(response: Response, errorMessage: String): DetailsPageDto = response.use { res ->
        res.extractNextJs<DetailsPageDto> { it.isDetailsPagePayload() }
            ?: throw IOException(errorMessage)
    }

    private fun JsonElement.isLibraryPagePayload(): Boolean {
        val payload = this as? JsonObject ?: return false
        val initialData = payload["initialData"] as? JsonObject ?: return false
        return "series" in initialData && "pagination" in initialData
    }

    private fun JsonElement.isLatestPagePayload(): Boolean {
        val payload = this as? JsonObject ?: return false
        val initialData = payload["initialData"] as? JsonObject ?: return false
        return "updates" in initialData && "pagination" in initialData
    }

    private fun JsonElement.isDetailsPagePayload(): Boolean {
        val payload = this as? JsonObject ?: return false
        val seriesData = payload["seriesData"] as? JsonObject ?: return false
        return "title" in seriesData && "description" in seriesData
    }

    private fun JsonElement.isReaderPagePayload(): Boolean {
        val payload = this as? JsonObject ?: return false
        val chapterData = payload["chapterData"] as? JsonObject ?: return false
        val chapter = chapterData["chapter"] as? JsonObject ?: return false
        return "pages" in chapter
    }

    private companion object {
        const val DEFAULT_SORT_BY = "total_views"
        const val DEFAULT_SORT_ORDER = "desc"
        const val CHAPTER_ORDER = "desc"
    }
}
