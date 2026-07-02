package eu.kanade.tachiyomi.extension.pt.lycantoons

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.annotation.Source
import keiyoushi.network.rateLimit
import keiyoushi.utils.extractNextJs
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonRequestBody
import okhttp3.Request
import okhttp3.Response

@Source
abstract class LycanToons : HttpSource() {

    override val supportsLatest = true

    override val client = network.client.newBuilder()
        .addInterceptor(WebViewInterceptor(baseUrl, headers["User-Agent"]))
        .rateLimit(2)
        .build()

    override fun headersBuilder() = super.headersBuilder()
        .add("Referer", "$baseUrl/")

    // =====================Popular=====================

    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/api/metrics/popular?limit=$PAGE_LIMIT&page=$page", headers)

    override fun popularMangaParse(response: Response): MangasPage = response.parseAs<SeriesListResponse>().toMangasPage()

    // =====================Latest=====================
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/api/chapters/recent?limit=50", headers)

    override fun latestUpdatesParse(response: Response): MangasPage = response.parseAs<LatestResponse>().toMangasPage()

    // =====================Search=====================
    override fun getFilterList(): FilterList = getFilters()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val seriesType = filters.valueOrEmpty<SeriesTypeFilter>().takeIf { it.isNotBlank() }
        val status = filters.valueOrEmpty<StatusFilter>().takeIf { it.isNotBlank() }
        val tags = filters.selectedTags().takeIf { it.isNotEmpty() }

        val payload = SearchPayload(
            page = page,
            limit = PAGE_LIMIT,
            search = query,
            seriesType = seriesType,
            status = status,
            tags = tags,
        )

        return POST("$baseUrl/api/series", headers, payload.toJsonRequestBody())
    }

    override fun searchMangaParse(response: Response): MangasPage = response.parseAs<SearchResponse>().toMangasPage()

    // =====================Details=====================
    override fun mangaDetailsRequest(manga: SManga): Request = GET(baseUrl + manga.url, headers)

    override fun mangaDetailsParse(response: Response): SManga = response.extractNextJs<SeriesDataWrapper>()?.seriesData?.toSManga()
        ?: throw Exception("Não foi possível extrair os detalhes da obra")

    // =====================Chapters=====================
    override fun chapterListRequest(manga: SManga): Request = GET(baseUrl + manga.url, headers)

    override fun chapterListParse(response: Response): List<SChapter> {
        val seriesData = response.extractNextJs<SeriesDataWrapper>()?.seriesData
            ?: throw Exception("Não foi possível extrair a lista de capítulos")

        return seriesData.capitulos?.map { it.toSChapter(seriesData.slug) } ?: emptyList()
    }

    // =====================Pages========================
    override fun pageListRequest(chapter: SChapter): Request = GET(baseUrl + chapter.url, headers)

    override fun pageListParse(response: Response): List<Page> {
        val pageList = response.extractNextJs<PageList>()
            ?: throw Exception("Não foi possível extrair a lista de páginas")

        return pageList.imageUrls.mapIndexed { i, url ->
            Page(i, imageUrl = url)
        }
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException("Not used.")

    companion object {
        private const val PAGE_LIMIT = 20
    }
}
