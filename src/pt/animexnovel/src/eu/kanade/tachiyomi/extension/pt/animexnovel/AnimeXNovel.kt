package eu.kanade.tachiyomi.extension.pt.animexnovel

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import java.util.concurrent.TimeUnit
import kotlin.collections.map
import kotlin.collections.plusAssign

class AnimeXNovel : HttpSource() {

    override val name = "AnimeXNovel"

    override val baseUrl: String = "https://www.animexnovel.com"

    override val lang: String = "pt-BR"

    override val supportsLatest: Boolean = true

    override val versionId: Int = 2

    override val client: OkHttpClient = network.client.newBuilder()
        .readTimeout(1, TimeUnit.MINUTES)
        .callTimeout(1, TimeUnit.MINUTES)
        .rateLimit(3, 1)
        .build()

    // ========================== Popular ===================================

    private val popularFilter = FilterList(
        listOf(
            BoxList("", setOf("Mangá", "Manhwa", "Manhua").map { BoxValue("", it) }).apply {
                state.forEach { it.state = true }
            },
        ),
    )

    override fun popularMangaRequest(page: Int): Request = searchMangaRequest(page, "", popularFilter)

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    // ========================== Latest ====================================

    override fun latestUpdatesRequest(page: Int): Request = GET(baseUrl)

    override fun latestUpdatesParse(response: Response): MangasPage = response.use {
        val mangas = it.asJsoup()
            .select("div:contains(Últimos Mangás) + .axn-piz-container .axn-piz-card")
            .map(::mangaFromElement)
        MangasPage(mangas, hasNextPage = false)
    }

    // ========================== Search ====================================

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val form = FormBody.Builder()
            .add("action", "axn_filter_obras")
            .add("posts_per_page", "21")
            .add("search", query)
            .add("paged", page.toString())
            .apply {
                filters.filterIsInstance<BoxList>()
                    .also { filterList ->
                        filterList.find { it.name.contains("ordem", ignoreCase = true) }
                            ?.state?.find(CheckBox::state)?.let {
                                if (it.id.isBlank()) return@let
                                add("letra", it.id)
                            }
                    }
                    .filterNot { it.name.contains("ordem", ignoreCase = true) }
                    .flatMap(BoxList::state)
                    .filter { it.state && it.id.isNotBlank() }
                    .forEach { filter -> add("terms[]", filter.id) }
            }
            .build()
        return POST("$baseUrl/wp-admin/admin-ajax.php", headers, form)
    }

    private var lastManga: SManga? = null

    override fun searchMangaParse(response: Response): MangasPage = response.use {
        val mangas = it.asJsoup().select("a.axn-card").map(::mangaFromElement).toMutableList()
        val hasNextPage = mangas.isNotEmpty() && mangas.size > 1

        when {
            hasNextPage -> {
                lastManga = mangas.removeAt(mangas.lastIndex)
            }
            else -> {
                lastManga?.let { mangas += it }
            }
        }

        MangasPage(mangas, hasNextPage = hasNextPage)
    }

    // ========================== Details ===================================

    override fun mangaDetailsParse(response: Response) = response.use {
        val document = it.asJsoup()
        val structuredData = document.selectFirst("script#axn-data")
            ?.data()
            ?.let { json -> runCatching { json.parseAs<JsonObject>() }.getOrNull() }
        val details = document.selectFirst(".wp-block-media-text:has(.wp-block-media-text__content)")

        SManga.create().apply {
            title = document.selectFirst(".eb-breadcrumb-item.current")?.text()
                ?: structuredData?.string("name")
                ?: document.selectFirst("h1.entry-title")!!.text()
            thumbnail_url = details?.selectFirst(".wp-block-media-text__media img")?.absUrl("src")
                ?: structuredData?.string("image")?.toHttps()
            author = details?.detailsValue("Autor")
                ?: structuredData?.nestedString("author", "name")
            artist = details?.detailsValue("Arte")
                ?: structuredData?.nestedString("illustrator", "name")
            genre = details?.detailsValue("Gênero")
                ?: structuredData?.string("genre")
            description = details?.select(".eb-accordion-content p")
                ?.joinToString("\n\n") { it.text() }
                ?.takeIf(String::isNotBlank)
                ?: structuredData?.string("description")
            structuredData?.string("creativeWorkStatus")?.let {
                status = when (it.lowercase()) {
                    "ongoing" -> SManga.ONGOING
                    "completed" -> SManga.COMPLETED
                    else -> SManga.UNKNOWN
                }
            }

            setUrlWithoutDomain(document.location())
        }
    }

    // ========================== Chapters ==================================

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> = Observable.fromCallable {
        val mangaUrl = manga.url.toAbsoluteUrl()
        val document = client.newCall(mangaDetailsRequest(manga))
            .execute()
            .use { it.asJsoup() }

        val category = document.selectFirst(".axn-sc-wrapper[data-categoria], .axn-chapters-container[data-categoria]")
            ?.attr("data-categoria")
            ?: return@fromCallable emptyList()

        val url = "$baseUrl/wp-json/axn/v1/chapters/$category".toHttpUrl().newBuilder()

        val chapterList = mutableListOf<SChapter>()
        var page = 1
        var totalPages: Int
        do {
            url.setQueryParameter("page", page.toString())
            val response = client.newCall(GET(url.build(), chapterListHeaders(mangaUrl))).execute()
            if (!response.isSuccessful) {
                response.close()
                break
            }
            totalPages = response.header("X-WP-TotalPages")?.toIntOrNull() ?: page
            chapterList += chapterListParse(response)
            page++
        } while (page <= totalPages)

        chapterList.distinctBy { it.url }.asReversed()
    }

    override fun chapterListParse(response: Response): List<SChapter> = response.use {
        it.parseAs<List<ChapterDto>>().mapNotNull { chapter -> chapter.toSChapter(baseUrl) }
    }

    // ========================== Pages =====================================

    private val pageImageSelector = listOf(
        ".spice-block-img-gallery img",
        ".wp-block-gallery img",
        ".spnc-entry-content img",
        "figure.wp-block-image img",
    ).joinToString()

    override fun pageListParse(response: Response): List<Page> = response.use {
        it.asJsoup().select(pageImageSelector).mapIndexed { index, element ->
            Page(index, imageUrl = element.absUrl("src"))
        }
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // =========================== Filters ==================================

    private class BoxList(title: String, values: List<BoxValue>) : Filter.Group<CheckBox>(title, values.map { CheckBox(it.name, it.id) })

    private class CheckBox(name: String, val id: String = name) : Filter.CheckBox(name)

    private class BoxValue(val name: String, val id: String = name)

    private var options: List<Pair<String, List<BoxValue>>> = emptyList()

    private val scope = CoroutineScope(Dispatchers.IO)

    private var fetchFiltersAttempts: Int = 0

    private fun fetchFilters() {
        if (fetchFiltersAttempts < 3 && options.isEmpty()) {
            try {
                options = client.newCall(filterRequest()).execute()
                    .use { parseOptions(it.asJsoup()) }
            } catch (_: Exception) {
            } finally {
                fetchFiltersAttempts++
            }
        }
    }

    private fun filterRequest(): Request = GET("$baseUrl/pesquisar", headers)

    private fun parseOptions(document: Document): List<Pair<String, List<BoxValue>>> {
        val filtersSelectors = setOf(
            "grp-alfabeto",
            "grp-demografia",
            "grp-classificacao",
            "grp-categorias",
            "grp-tags",
        )
        return filtersSelectors.mapNotNull { selector ->
            val title = document.selectFirst(".filter-section-title:has( + #$selector)")?.text()
                ?: return@mapNotNull null
            title to document.select("#$selector .axn-chip").map { element ->
                BoxValue(element.text(), element.attr("data-value"))
            }
        }
    }

    override fun getFilterList(): FilterList {
        scope.launch { fetchFilters() }

        val filters: MutableList<Filter<out Any>> = mutableListOf()

        if (options.isNotEmpty()) {
            options.forEachIndexed { index, (title, values) ->
                if (index != 0) {
                    filters += Filter.Separator()
                }
                filters += BoxList(title, values)
            }
        } else {
            filters += listOf(
                Filter.Separator(),
                Filter.Header("Aperte 'Redefinir' para tentar mostrar os filtros"),
            )
        }
        return FilterList(filters)
    }

    // =========================== Utils ====================================

    private fun chapterListHeaders(referer: String) = headersBuilder()
        .set("Accept", "application/json,*/*")
        .set("Referer", referer)
        .set("Sec-Fetch-Dest", "empty")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    private fun String.toAbsoluteUrl(): String = if (startsWith("http")) this else baseUrl + this

    private fun String.toHttps() = replace("http://", "https://")

    private fun JsonObject.string(key: String): String? = get(key)?.jsonPrimitive?.contentOrNull

    private fun JsonObject.nestedString(key: String, nestedKey: String): String? = get(key)
        ?.jsonObject
        ?.string(nestedKey)

    private fun Element.detailsValue(label: String): String? {
        return select(".wp-block-media-text__content > ul.wp-block-list > li").firstNotNullOfOrNull { item ->
            val text = item.text()
            val itemLabel = text.substringBefore(":").trim()
            if (!itemLabel.equals(label, ignoreCase = true)) return@firstNotNullOfOrNull null

            text.substringAfter(":")
                .trim()
                .takeIf(String::isNotBlank)
        }
    }

    private fun mangaFromElement(element: Element): SManga = SManga.create().apply {
        title = element.selectFirst("h2, h3, .search-content")!!.text()
        thumbnail_url = element.selectFirst("img")?.absUrl("src")
        setUrlWithoutDomain(element.selectFirst("a")!!.absUrl("href"))
    }
}
