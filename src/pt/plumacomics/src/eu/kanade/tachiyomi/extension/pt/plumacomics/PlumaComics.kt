package eu.kanade.tachiyomi.extension.pt.plumacomics

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.utils.extractNextJs
import keiyoushi.utils.parseAs
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlumaComics : HttpSource() {

    override val name: String = "Pluma Comics"

    override val lang: String = "pt-BR"

    override val baseUrl: String = "https://plumacomics.cloud"

    override val supportsLatest: Boolean = true

    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val versionId = 5

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun headersBuilder() = super.headersBuilder()
        .set("Referer", "$baseUrl/")
        .set("Accept-Encoding", "gzip")

    // Popular

    override fun popularMangaRequest(page: Int): Request {
        val url = "$baseUrl/series".toHttpUrl().newBuilder()
            .addQueryParameter("sort", "popular")
            .apply {
                if (page > 1) {
                    addQueryParameter("page", page.toString())
                }
            }
            .build()

        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage = response.use {
        val document = it.asJsoup()
        val payload = document.extractNextJs<JsonObject>(JsonElement::isCatalogGridPayload)
            ?: throw IOException("Nao foi possivel extrair o catalogo popular")

        val currentPage = it.request.url.queryParameter("page")?.toIntOrNull() ?: 1
        MangasPage(payload.toCatalogMangaList(baseUrl), document.hasNextPage("page", currentPage))
    }

    // Latest

    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/?aba=$page", headers)

    override fun latestUpdatesParse(response: Response): MangasPage = response.use {
        val document = it.asJsoup()
        val payload = document.extractNextJs<LatestPayloadDto>(JsonElement::isLatestPayload)
            ?: throw IOException("Nao foi possivel extrair as atualizacoes")

        val currentPage = it.request.url.queryParameter("aba")?.toIntOrNull() ?: 1
        MangasPage(payload.toMangaList(baseUrl), document.hasNextPage("aba", currentPage))
    }

    // Search

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/api/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .build()
        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = response.use {
        MangasPage(it.parseAs<SearchDto>().toMangaList(baseUrl), hasNextPage = false)
    }

    // Details

    override fun mangaDetailsParse(response: Response): SManga = response.use {
        val document = it.asJsoup()
        val payload = document.extractNextJs<JsonObject>(JsonElement::isSeriesDetailsPayload)
            ?: throw IOException("Nao foi possivel extrair os detalhes da obra")

        payload.toDetailsManga(baseUrl, it.request.url.encodedPath)
    }

    // Chapters

    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        val payload = it.asJsoup().extractNextJs<ChapterListDto>(JsonElement::isChapterListPayload)
            ?: throw IOException("Nao foi possivel extrair os capitulos")

        payload.toSChapterList(dateFormat)
    }

    // Pages

    override fun pageListRequest(chapter: SChapter): Request {
        if (chapter.url.queryParameterValue("vip") == 1 && !chapter.url.isVipChapterUnlocked()) {
            throw IOException(VIP_CHAPTER_ERROR)
        }

        val chapterId = chapter.url.extractChapterId()
        val requestHeaders = headers.newBuilder()
            .set("Referer", "$baseUrl/ler/$chapterId")
            .build()

        return GET("$baseUrl/api/viewer/bootstrap?c=$chapterId", requestHeaders)
    }

    override fun pageListParse(response: Response): List<Page> = response.use {
        it.parseAs<ViewerBootstrapDto>()
            .toPageList(baseUrl, it.request.header("Referer") ?: "$baseUrl/")
            .also { pages ->
                if (pages.isEmpty()) {
                    throw IOException("Nenhuma pagina encontrada para este capitulo")
                }
            }
    }

    override fun imageRequest(page: Page): Request {
        val imageHeaders = headers.newBuilder()
            .set("Referer", page.url.takeIf(String::isNotEmpty) ?: "$baseUrl/")
            .build()

        return GET(page.imageUrl!!, imageHeaders)
    }

    override fun getChapterUrl(chapter: SChapter): String = "$baseUrl/ler/${chapter.url.extractChapterId()}"

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    private fun Document.hasNextPage(parameter: String, currentPage: Int): Boolean = select("a.btn-primary[href]").any {
        it.attr("href").queryParameterValue(parameter)?.let { targetPage ->
            targetPage > currentPage
        } == true
    }

    private fun String.queryParameterValue(parameter: String): Int? {
        val query = substringAfter("?", missingDelimiterValue = "")
            .substringBefore("#")
        return query.split("&")
            .firstOrNull { it.substringBefore("=") == parameter }
            ?.substringAfter("=", missingDelimiterValue = "")
            ?.toIntOrNull()
    }

    private fun String.queryParameterLong(parameter: String): Long? {
        val query = substringAfter("?", missingDelimiterValue = "")
            .substringBefore("#")
        return query.split("&")
            .firstOrNull { it.substringBefore("=") == parameter }
            ?.substringAfter("=", missingDelimiterValue = "")
            ?.toLongOrNull()
    }

    private fun String.isVipChapterUnlocked(): Boolean {
        val unlockAt = queryParameterLong(VIP_UNLOCK_AT_QUERY)
        return unlockAt != null && unlockAt <= System.currentTimeMillis()
    }

    private fun String.extractChapterId(): String = when {
        startsWith("/api/viewer/bootstrap") -> substringAfter("c=").substringBefore("&")
        startsWith("/ler/") -> substringAfterLast("/")
        else -> substringAfterLast("/")
    }
}
