package eu.kanade.tachiyomi.extension.pt.valkyuri

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.annotation.Source
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.parseAs
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response

@Source
abstract class ValkYuri : HttpSource() {

    override val supportsLatest = true

    private val apiUrl = "https://nexus.valkyuri.com/api"

    override fun headersBuilder() = super.headersBuilder()
        .add("Accept", "application/json")
        .add("Referer", "$baseUrl/")
        .add("Origin", baseUrl)

    override fun popularMangaRequest(page: Int): Request {
        val url = discoveryUrl(POPULAR_SECTION, SECTION_LIMIT)
        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage = response.use {
        it.parseAs<HomeDiscoveryResponseDto>().toMangasPage(POPULAR_SECTION)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$apiUrl/releases/latest".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("per_page", PAGE_SIZE.toString())
            .build()

        return GET(url, headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = response.use {
        it.parseAs<LatestReleasesResponseDto>().toMangasPage()
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val normalizedQuery = query.trim()
        val url = if (normalizedQuery.isNotEmpty()) {
            "$apiUrl/search/mangas".toHttpUrl().newBuilder()
                .addQueryParameter("q", normalizedQuery)
                .addQueryParameter("page", page.toString())
                .addQueryParameter("per_page", PAGE_SIZE.toString())
                .build()
        } else {
            val section = filters.firstInstanceOrNull<SectionFilter>()?.selected ?: POPULAR_SECTION
            if (section == CATALOG_SECTION) {
                val sort = filters.firstInstanceOrNull<CatalogSortFilter>()?.selected ?: "latest"
                catalogUrl(sort, page)
            } else {
                discoveryUrl(section, SECTION_LIMIT)
            }
        }

        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = response.use {
        if (it.request.url.encodedPath.endsWith("/home/discovery")) {
            val section = it.request.url.queryParameter("section") ?: POPULAR_SECTION
            it.parseAs<HomeDiscoveryResponseDto>().toMangasPage(section)
        } else {
            it.parseAs<MangaListResponseDto>().toMangasPage()
        }
    }

    override fun getMangaUrl(manga: SManga): String = "$baseUrl/media/${manga.url}"

    override fun mangaDetailsRequest(manga: SManga): Request {
        val url = "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addPathSegment(manga.url)
            .build()

        return GET(url, headers)
    }

    override fun mangaDetailsParse(response: Response): SManga = response.use {
        it.parseAs<MangaDetailsResponseDto>().toSManga()
    }

    override fun chapterListRequest(manga: SManga): Request = mangaDetailsRequest(manga)

    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        it.parseAs<MangaDetailsResponseDto>().toChapterList()
    }

    override fun getChapterUrl(chapter: SChapter): String = "$baseUrl${chapter.url}"

    override fun pageListRequest(chapter: SChapter): Request {
        val chapterUrl = getChapterUrl(chapter).toHttpUrl()
        val pathSegments = chapterUrl.pathSegments
        val mangaSlug = pathSegments.getOrNull(1).orEmpty()
        val chapterNumber = pathSegments.getOrNull(2).orEmpty()

        val url = "$apiUrl/mangas".toHttpUrl().newBuilder()
            .addPathSegment(mangaSlug)
            .addPathSegment("chapters")
            .addPathSegment(chapterNumber)
            .build()

        return GET(url, headers)
    }

    override fun pageListParse(response: Response): List<Page> = response.use {
        it.parseAs<ChapterDetailsResponseDto>().toPages()
    }

    override fun imageRequest(page: Page): Request {
        val imageUrl = page.imageUrl ?: page.url
        return GET(imageUrl, headers)
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    override fun getFilterList() = FilterList(
        SectionFilter(),
        CatalogSortFilter(),
    )

    private fun catalogUrl(sort: String, page: Int): HttpUrl = "$apiUrl/mangas".toHttpUrl().newBuilder()
        .addQueryParameter("sort", sort)
        .addQueryParameter("page", page.toString())
        .addQueryParameter("per_page", PAGE_SIZE.toString())
        .build()

    private fun discoveryUrl(section: String, limit: Int): HttpUrl = "$apiUrl/home/discovery".toHttpUrl().newBuilder()
        .addQueryParameter("limit", limit.toString())
        .addQueryParameter("section", section)
        .build()

    companion object {
        private const val PAGE_SIZE = 24
        private const val SECTION_LIMIT = 50
        private const val POPULAR_SECTION = "popular_week"
    }
}
