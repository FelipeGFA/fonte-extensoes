package eu.kanade.tachiyomi.extension.pt.lycantoons

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.network.rateLimit
import keiyoushi.utils.extractNextJs
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonRequestBody
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import okhttp3.Request
import okhttp3.Response

class LycanToons : HttpSource() {

    override val name = "Lycan Toons"

    override val baseUrl = "https://lycantoons.com"

    override val lang = "pt-BR"

    override val supportsLatest = true

    override val client = network.client.newBuilder()
        .addInterceptor(WebViewFetchProxy(baseUrl))
        .addInterceptor(ImageProxy(CDN_URL))
        .rateLimit(2)
        .build()

    private val apiHeaders by lazy {
        headers.newBuilder()
            .set("Accept", "application/json")
            .set("Referer", "$baseUrl/")
            .build()
    }

    // =====================Popular=====================

    override fun popularMangaRequest(page: Int): Request = metricsRequest("popular", page)

    override fun popularMangaParse(response: Response): MangasPage = response.use {
        it.parseAs<PopularResponse>().toMangasPage()
    }

    // =====================Latest=====================

    override fun latestUpdatesRequest(page: Int): Request = metricsRequest("recently-updated", page)

    override fun latestUpdatesParse(response: Response): MangasPage = response.use {
        it.parseAs<PopularResponse>().toMangasPage()
    }

    // =====================Search=====================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val payload = SearchRequestBody(
            limit = PAGE_LIMIT,
            page = page,
            search = query,
            seriesType = filters.valueOrEmpty<SeriesTypeFilter>(),
            status = filters.valueOrEmpty<StatusFilter>(),
            tags = filters.selectedTags(),
        )

        return POST("$baseUrl/api/series", apiHeaders, payload.toJsonRequestBody())
    }

    override fun searchMangaParse(response: Response): MangasPage = response.use {
        it.parseAs<SearchResponse>().toMangasPage()
    }

    override fun getFilterList(): FilterList = Filters.get()

    // =====================Details=====================

    override fun getMangaUrl(manga: SManga): String = "$baseUrl${manga.url}"

    override fun mangaDetailsRequest(manga: SManga): Request = seriesRequest(manga.slug())

    override fun mangaDetailsParse(response: Response): SManga = response.use {
        it.parseAs<SeriesDto>().toSManga()
    }

    // =====================Chapters=====================

    override fun chapterListRequest(manga: SManga): Request = seriesRequest(manga.slug())

    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        it.parseAs<SeriesDto>().toChapterList()
    }

    // =====================Pages========================

    override fun getChapterUrl(chapter: SChapter): String = "$baseUrl${chapter.url.substringBefore("?")}"

    override fun pageListRequest(chapter: SChapter): Request {
        val chapterUrl = getChapterUrl(chapter)

        val requestHeaders = headers.newBuilder()
            .set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .set("Referer", chapterUrl)
            .build()

        return GET(chapterUrl, requestHeaders)
    }

    override fun pageListParse(response: Response): List<Page> = response.use {
        val dto = it.extractNextJs<ChapterPageDto> { element ->
            element is JsonObject && element["imageUrls"] is JsonArray
        } ?: throw IllegalStateException("Nenhuma pagina encontrada para este capitulo")

        dto.toPageList()
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // =====================Utils=====================

    private fun metricsRequest(path: String, page: Int): Request = GET("$baseUrl/api/metrics/$path?limit=$PAGE_LIMIT&page=$page", apiHeaders)

    private fun seriesRequest(slug: String): Request = GET("$baseUrl/api/series/$slug", apiHeaders)

    private fun SManga.slug(): String = url.substringAfterLast("/")

    companion object {
        private const val CDN_URL = "https://cdn.lycantoons.com"
        private const val PAGE_LIMIT = 13
    }
}
