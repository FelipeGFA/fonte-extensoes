package eu.kanade.tachiyomi.extension.pt.sakuramangas

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.utils.parseAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SakuraMangas : HttpSource() {
    override val lang = "pt-BR"
    override val supportsLatest = true
    override val name = "Sakura Mangás"
    override val baseUrl = "https://sakuramangas.org"

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.6878.39 Mobile Safari/537.36"
        const val SEC_CH_UA = "\"Not A(Brand\";v=\"8\", \"Chromium\";v=\"144\", \"Google Chrome\";v=\"144\""
    }

    private val apiClient = network.cloudflareClient.newBuilder().rateLimit(2).build()
    override val client = network.cloudflareClient.newBuilder().rateLimit(2).build()

    private val webViewInterceptor by lazy { WebViewInterceptor(USER_AGENT, client, baseUrl) }

    private var genresSet: Set<Genre> = emptySet()
    private var demographyOptions = listOf("Todos" to "")
    private var classificationOptions = listOf("Todos" to "")
    private var orderByOptions = listOf("Lidos" to "3")
    private var fetchFiltersAttempts = 0
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun launchIO(block: () -> Unit) = scope.launch { block() }

    private class WebViewDebugException :
        IOException(
            "Fluxo de depuração WebView ativo. Verifique o Logcat com a TAG SakuraWebViewFlow.",
        )

    // ================================ Headers =======================================

    private fun baseHeaders() = Headers.Builder()
        .set("User-Agent", USER_AGENT)
        .set("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
        .set("Cache-Control", "no-cache")
        .set("Pragma", "no-cache")
        .set("Sec-CH-UA", SEC_CH_UA)
        .set("Sec-CH-UA-Mobile", "?1")
        .set("Sec-CH-UA-Platform", "\"Android\"")
        .set("DNT", "1")

    override fun headersBuilder() = baseHeaders()
        .set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
        .set("Priority", "u=0, i")
        .set("Sec-Fetch-Dest", "document")
        .set("Sec-Fetch-Mode", "navigate")
        .set("Sec-Fetch-Site", "none")
        .set("Sec-Fetch-User", "?1")
        .set("Upgrade-Insecure-Requests", "1")

    private fun ajaxHeaders(referer: String) = baseHeaders()
        .set("Accept", "*/*")
        .set("Origin", baseUrl)
        .set("Priority", "u=1, i")
        .set("Referer", referer)
        .set("X-Requested-With", "XMLHttpRequest")
        .set("Sec-Fetch-Dest", "empty")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    // ================================ Requests =======================================

    override fun mangaDetailsRequest(manga: SManga) = GET(baseUrl + manga.url, headers)
    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)
    override fun pageListRequest(chapter: SChapter) = GET(baseUrl + chapter.url, headers)

    override fun imageRequest(page: Page): Request = GET(page.imageUrl!!, headers)

    override fun getMangaUrl(manga: SManga) = "$baseUrl${manga.url}"

    // ================================ Popular =======================================

    override fun popularMangaRequest(page: Int) = GET("$baseUrl/dist/sakura/models/home/__.home_maislidos.php", ajaxHeaders("$baseUrl/"))

    override fun fetchPopularManga(page: Int) = apiClient.newCall(popularMangaRequest(page)).asObservableSuccess().map { popularMangaParse(it) }

    override fun popularMangaParse(response: Response): MangasPage {
        val mangas = response.parseAs<SakuraMangasPopularResponseDto>().data.map { it.toSManga(baseUrl) }
        return MangasPage(mangas, hasNextPage = false)
    }

    // ================================ Latest =======================================

    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/dist/sakura/models/home/__.home_ultimos.php", ajaxHeaders("$baseUrl/"))

    override fun fetchLatestUpdates(page: Int) = apiClient.newCall(latestUpdatesRequest(page)).asObservableSuccess().map { latestUpdatesParse(it) }

    override fun latestUpdatesParse(response: Response): MangasPage {
        val mangas = response.parseAs<List<SakuraMangasLatestDto>>().map { it.toSManga(baseUrl) }
        return MangasPage(mangas, hasNextPage = false)
    }

    // ================================ Search =======================================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val form = FormBody.Builder().apply {
            add("search", query)
            add("order", "3")
            add("offset", ((page - 1) * 30).toString())
            add("limit", "30")

            filters.forEach { filter ->
                when (filter) {
                    is GenreList -> filter.state.forEach {
                        when (it.state) {
                            Filter.TriState.STATE_INCLUDE -> add("tags[]", it.id)
                            Filter.TriState.STATE_EXCLUDE -> add("excludeTags[]", it.id)
                            else -> {}
                        }
                    }
                    is DemographyFilter -> filter.getValue().takeIf { it.isNotEmpty() }?.let { add("demography", it) }
                    is ClassificationFilter -> filter.getValue().takeIf { it.isNotEmpty() }?.let { add("classification", it) }
                    is OrderByFilter -> filter.getValue().takeIf { it.isNotEmpty() }?.let { add("order", it) }
                    else -> {}
                }
            }
        }
        return POST("$baseUrl/dist/sakura/models/obras/__.obras__buscar.php", ajaxHeaders("$baseUrl/obras/"), form.build())
    }

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList) = apiClient.newCall(searchMangaRequest(page, query, filters)).asObservableSuccess().map { searchMangaParse(it) }

    override fun searchMangaParse(response: Response): MangasPage {
        val result = response.parseAs<SakuraMangasSearchResponseDto>()
        return MangasPage(result.data.map { it.toSManga(baseUrl) }, result.hasMore)
    }

    // ================================ Details =======================================

    override fun fetchMangaDetails(manga: SManga) = client.newCall(mangaDetailsRequest(manga)).asObservableSuccess().map { mangaDetailsParse(it) }

    override fun mangaDetailsParse(response: Response): SManga {
        val url = response.request.url.toString()
        response.close()
        webViewInterceptor.inspectPage(url, "manga-details")
        throw WebViewDebugException()
    }

    // ================================ Chapters =======================================

    override fun fetchChapterList(manga: SManga) = client.newCall(chapterListRequest(manga)).asObservableSuccess().map { chapterListParse(it) }

    override fun chapterListParse(response: Response): List<SChapter> {
        response.close()
        return emptyList()
    }

    // ================================ Pages =======================================

    override fun pageListParse(response: Response): List<Page> {
        response.close()
        return emptyList()
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // ================================ Filters =======================================

    override fun getFilterList(): FilterList {
        launchIO { fetchFilters() }

        val filters = mutableListOf<Filter<*>>()
        if (genresSet.isEmpty()) {
            filters += Filter.Header("Clique em 'Redefinir' para carregar os filtros")
            filters += Filter.Separator()
        }
        filters += OrderByFilter("Ordenar por", orderByOptions)
        filters += DemographyFilter("Demografia", demographyOptions)
        filters += ClassificationFilter("Classificação", classificationOptions)
        filters += GenreList("Gêneros", genresSet.toTypedArray())

        return FilterList(filters)
    }

    private fun fetchFilters() {
        if (genresSet.isNotEmpty() || fetchFiltersAttempts >= 3) return
        fetchFiltersAttempts++

        runCatching {
            val document = client.newCall(GET("$baseUrl/obras/", headers)).execute().asJsoup()

            val genres = document.select("#generos-badges .genre-chip").map { Genre(it.text(), it.attr("data-value")) }
            val themes = document.select("#temas-badges .genre-chip").map { Genre(it.text(), it.attr("data-value")) }
            genresSet = (genres + themes).toSet()

            document.select("#group-demografia .btn-filter-chip").mapNotNull { el ->
                val value = el.attr("data-value")
                val text = el.text()
                if (text.isEmpty()) null else text to value
            }.takeIf { it.isNotEmpty() }?.let { demographyOptions = it }

            document.select("#group-classificacao .btn-filter-chip").mapNotNull { el ->
                val value = el.attr("data-value")
                val text = el.text()
                if (text.isEmpty()) null else text to value
            }.takeIf { it.isNotEmpty() }?.let { classificationOptions = it }

            document.select("#group-ordenacao .btn-sort-option").mapNotNull { el ->
                val value = el.attr("data-value")
                val text = el.text()
                if (text.isEmpty()) null else text to value
            }.takeIf { it.isNotEmpty() }?.let { orderByOptions = it }
        }
    }
}
