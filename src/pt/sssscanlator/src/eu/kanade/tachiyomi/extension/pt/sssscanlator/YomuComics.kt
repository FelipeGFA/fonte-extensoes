package eu.kanade.tachiyomi.extension.pt.sssscanlator

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.network.rateLimit
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.parseAs
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class YomuComics : HttpSource() {

    override val name = "Yomu Comics"

    override val baseUrl = "https://yomu.com.br"

    override val lang = "pt-BR"

    override val supportsLatest = true

    // SSSScanlator
    override val id = 1497838059713668619

    override val client: OkHttpClient = network.client.newBuilder()
        .rateLimit(5)
        .build()

    override fun headersBuilder() = super.headersBuilder()
        .add("Referer", "$baseUrl/")

    // Popular

    override fun popularMangaRequest(page: Int): Request {
        val url = "$baseUrl/api/library".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", PAGE_SIZE.toString())
            .addQueryParameter("sort", "popular")
            .addQueryParameter("type", DEFAULT_TYPE)
            .build()

        return GET(url, bibliotecaHeaders)
    }

    override fun popularMangaParse(response: Response): MangasPage = parseLibraryResponse(response)

    // Latest

    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$baseUrl/api/library".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", PAGE_SIZE.toString())
            .addQueryParameter("sort", "recent")
            .addQueryParameter("type", DEFAULT_TYPE)
            .build()

        return GET(url, bibliotecaHeaders)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = parseLibraryResponse(response)

    // Search

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val genre = filters.firstInstanceOrNull<GenreFilter>()?.selectedValue.orEmpty()
        val type = filters.firstInstanceOrNull<TypeFilter>()?.selectedValue ?: DEFAULT_TYPE
        val status = filters.firstInstanceOrNull<StatusFilter>()?.selectedValue ?: DEFAULT_STATUS
        val sort = filters.firstInstanceOrNull<SortFilter>()?.selectedValue ?: DEFAULT_SORT

        val url = "$baseUrl/api/library".toHttpUrl().newBuilder().apply {
            addQueryParameter("page", page.toString())
            addQueryParameter("limit", PAGE_SIZE.toString())
            addQueryParameter("sort", sort)
            addQueryParameter("type", type)

            if (genre.isNotBlank()) {
                addQueryParameter("genre", genre)
            }

            if (status != DEFAULT_STATUS) {
                addQueryParameter("status", status)
            }

            if (query.isNotBlank()) {
                addQueryParameter("search", query)
            }
        }.build()

        return GET(url, bibliotecaHeaders)
    }

    override fun searchMangaParse(response: Response): MangasPage = parseLibraryResponse(response)

    // Details

    override fun getMangaUrl(manga: SManga): String = baseUrl + manga.url

    override fun mangaDetailsParse(response: Response): SManga = parseSeriesPage(response).manga

    override fun getChapterUrl(chapter: SChapter): String = baseUrl + chapter.url.substringBefore('?')

    // Chapters

    override fun chapterListParse(response: Response): List<SChapter> = parseSeriesPage(response).chapters

    // Pages

    override fun pageListRequest(chapter: SChapter): Request {
        val chapterPageUrl = getChapterUrl(chapter)

        val requestHeaders = headers.newBuilder()
            .set("Referer", chapterPageUrl)
            .build()

        return GET(chapterPageUrl, requestHeaders)
    }

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()

        val html = document.html()
        val base64Str = html.substringAfter("U2FsdGVkX1", missingDelimiterValue = "")
            .substringBefore('"')
            .let { if (it.isNotEmpty()) "U2FsdGVkX1$it" else null }

        val images = base64Str?.let {
            runCatching { it.decryptAndParseAs<ChapterImagesDto>() }
                .getOrNull()
                ?.images
                ?.takeIf(List<String>::isNotEmpty)
        }

        if (images != null) {
            return images.mapIndexed { index, imageUrl ->
                Page(index, imageUrl = imageUrl)
            }
        }

        error("Nenhuma pagina encontrada para este capitulo")
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    override fun imageRequest(page: Page): Request {
        val requestHeaders = headers.newBuilder()
            .set("Referer", "$baseUrl/")
            .build()
        return GET(page.imageUrl!!, requestHeaders)
    }

    // Filters

    override fun getFilterList() = FilterList(
        GenreFilter(),
        TypeFilter(),
        StatusFilter(),
        SortFilter(),
    )

    // Utils

    private fun parseLibraryResponse(response: Response): MangasPage {
        val dto = response.parseAs<LibraryResponseDto>()

        val mangasList = dto.garimpo?.let { base64Str ->
            base64Str.decryptAndParseAs<List<LibraryMangaDto>>()
        } ?: emptyList()

        val mangas = mangasList.filterNot(LibraryMangaDto::isNovel).map(LibraryMangaDto::toSManga)
        val hasNextPage = dto.pagination.page < dto.pagination.totalPages
        return MangasPage(mangas, hasNextPage)
    }

    private fun parseSeriesPage(response: Response): SeriesPageData {
        val mangaSlug = response.request.url.pathSegments.lastOrNull().orEmpty()
        val document = response.asJsoup()
        val chaptersList = extractSeriesChapters(document, mangaSlug)

        val titleElement = document.selectFirst("h1")
        val title = titleElement?.text().orEmpty()
        val badgeTexts = extractBadgeTexts(titleElement)
        val statusText = badgeTexts.firstOrNull(::isStatusBadge)
        val genres = badgeTexts.filterNot(::isStatusBadge)

        val coverUrl = document.selectFirst("meta[property=og:image]")?.attr("content")
        val descriptionText = document.selectFirst("meta[property=og:description]")?.attr("content")
            ?: document.selectFirst("meta[name=description]")?.attr("content")
            ?: document.selectFirst("div.lg\\:col-span-8 p.text-muted-foreground")?.text()

        var authorText: String? = null
        var artistText: String? = null
        document.select("div.flex.items-center.gap-2.text-sm").forEach { el ->
            val text = el.text()
            if (text.contains("Autor", ignoreCase = true)) {
                authorText = text.substringAfter("Autor:").substringAfter("Autor").trim()
            } else if (text.contains("Artista", ignoreCase = true)) {
                artistText = text.substringAfter("Artista:").substringAfter("Artista").trim()
            }
        }

        val manga = SManga.create().apply {
            this.title = title
            thumbnail_url = coverUrl?.takeUnless(String::isBlank)
            description = descriptionText?.takeUnless(String::isBlank)
            author = authorText?.takeUnless(String::isBlank)
            artist = artistText?.takeUnless(String::isBlank)
            genre = genres.joinToString().takeUnless(String::isBlank)
            status = parseStatus(statusText)
            url = "/obra/$mangaSlug"
            initialized = true
        }

        val chapters = chaptersList.map { chapter ->
            chapter.toSChapter(mangaSlug)
        }

        return SeriesPageData(manga, chapters)
    }

    private class SeriesPageData(
        val manga: SManga,
        val chapters: List<SChapter>,
    )

    private companion object {
        const val PAGE_SIZE = 20
        const val DEFAULT_TYPE = "all"
        const val DEFAULT_STATUS = "all"
        const val DEFAULT_SORT = "popular"
    }

    private val bibliotecaHeaders by lazy {
        headers.newBuilder()
            .set("Referer", "$baseUrl/biblioteca")
            .build()
    }
}
