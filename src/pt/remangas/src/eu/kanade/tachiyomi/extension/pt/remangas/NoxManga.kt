package eu.kanade.tachiyomi.extension.pt.remangas

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.network.rateLimit
import keiyoushi.utils.parseAs
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.time.Duration.Companion.seconds

class NoxManga : HttpSource() {

    override val name: String = "NoxManga"

    override val baseUrl: String = "https://noxtoons.com"

    override val lang: String = "pt-BR"

    override val supportsLatest: Boolean = true

    override val id: Long = 7462657023971681136

    override val client: OkHttpClient = network.client.newBuilder()
        .addInterceptor(NixSignatureInterceptor(network.client, API_ORIGIN, baseUrl, SITE_ID))
        .rateLimit(3, 1.seconds)
        .build()

    private val apiUrl: String = "$API_ORIGIN/api/v1/comics"

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .set("Origin", baseUrl)
        .set("Referer", "$baseUrl/")

    // ====================== Popular ====================================

    override fun popularMangaRequest(page: Int): Request {
        val url = apiUrl.toHttpUrl().newBuilder()
            .addQueryParameter("per_page", "24")
            .addQueryParameter("sort", "popular")
            .addQueryParameter("period", "week")
            .addQueryParameter("page", page.toString())
            .build()
        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage = response.use {
        val dto = it.parseAs<PageableDto<MangaDto>>()
        val mangas = dto.list.map(MangaDto::toSManga)
        MangasPage(mangas, hasNextPage = dto.hasNextPage())
    }

    // ====================== Latest ====================================

    override fun latestUpdatesRequest(page: Int): Request {
        val url = apiUrl.toHttpUrl().newBuilder()
            .addQueryParameter("per_page", "24")
            .addQueryParameter("sort", "latest")
            .addQueryParameter("period", "week")
            .addQueryParameter("page", page.toString())
            .build()
        return GET(url, headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    // ====================== Search ====================================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$apiUrl/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("page", page.toString())
            .build()
        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = popularMangaParse(response)

    // ====================== Details ====================================

    override fun mangaDetailsParse(response: Response): SManga = response.use {
        val document = it.asJsoup()
        SManga.create().apply {
            title = document.selectFirst(".detail-title")!!.text()
            thumbnail_url = document.selectFirst(".detail-cover img")?.absUrl("src")
            description = document.selectFirst(".detail-description")?.text()
            genre = document.select(".detail-tags a").joinToString { it.text() }
            document.selectFirst(".status-badge")?.text()?.let {
                status = when (it.lowercase()) {
                    "em andamento" -> SManga.ONGOING
                    "hiato" -> SManga.ONGOING
                    "completo" -> SManga.COMPLETED
                    "cancelado" -> SManga.CANCELLED
                    else -> SManga.UNKNOWN
                }
            }
        }
    }

    // ====================== Chapters ====================================

    override fun getMangaUrl(manga: SManga): String = "$baseUrl${manga.url}"

    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        val document = it.asJsoup()
        val dto = document.select("script[type=application/ld+json]")
            .firstNotNullOfOrNull { element ->
                val json = element.data().ifEmpty { element.html() }
                if (!json.contains("ItemList") || !json.contains("itemListElement")) {
                    return@firstNotNullOfOrNull null
                }

                runCatching { json.parseAs<ChapterListJsonLdDto>() }.getOrNull()
            } ?: return@use emptyList()

        dto.toChapterList { setUrlWithoutDomain(it) }
    }

    // ====================== Pages ====================================

    override fun getChapterUrl(chapter: SChapter): String = "$baseUrl${chapter.url}"

    override fun pageListParse(response: Response): List<Page> = response.use {
        val pages = it.asJsoup()
            .select("""img[src*="/chapters/"]""")
            .mapNotNull { element -> element.absUrl("src").takeIf(String::isNotEmpty) }
            .distinct()
            .mapIndexed { index, imageUrl -> Page(index, imageUrl = imageUrl) }

        pages
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException()

    companion object {
        private const val API_ORIGIN = "https://xodneo.site"
        private const val SITE_ID = "00000000-0000-0000-0000-000000000003"
    }
}
