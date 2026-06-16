package eu.kanade.tachiyomi.extension.pt.inkscan

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.network.rateLimit
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonRequestBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class InkScan : HttpSource() {

    override val name = "Ink Scan"

    override val baseUrl = "https://inkscann.live"

    override val lang = "pt-BR"

    override val supportsLatest = true

    private val apiUrl = "https://delicate-hill-05c1inkscan.inkscann.workers.dev"
    private val apiHost = apiUrl.toHttpUrl().host

    override val client: OkHttpClient = network.client.newBuilder()
        .rateLimit(3, 1.seconds) { it.host == apiHost }
        .build()

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .set("Accept", "application/json, text/plain, */*")
        .set("Origin", baseUrl)
        .set("Referer", "$baseUrl/")

    private val restHeaders: Headers by lazy {
        apiHeadersBuilder()
            .set("accept-profile", "public")
            .set("Prefer", "count=exact")
            .build()
    }

    private val rpcHeaders: Headers by lazy {
        apiHeadersBuilder()
            .set("content-profile", "public")
            .build()
    }

    private val functionHeaders: Headers by lazy {
        apiHeadersBuilder().build()
    }

    // ============================= Popular ================================

    override fun popularMangaRequest(page: Int): Request = worksRequest(page, POPULAR_SORT, FilterList())

    override fun popularMangaParse(response: Response): MangasPage = response.toWorksPage()

    // ============================= Latest =================================

    override fun latestUpdatesRequest(page: Int): Request = worksRequest(page, LATEST_SORT, FilterList())

    override fun latestUpdatesParse(response: Response): MangasPage = response.toWorksPage()

    // ============================= Search =================================

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        if (query.isBlank()) {
            return super.fetchSearchManga(page, query, filters)
        }

        return Observable.fromCallable {
            val ids = client.newCall(searchIdsRequest(query)).execute().use { response ->
                response.parseAs<List<SearchResultDto>>().map { it.id() }
            }

            if (ids.isEmpty()) {
                return@fromCallable MangasPage(emptyList(), false)
            }

            val works = client.newCall(worksByIdsRequest(ids, filters)).execute().use { response ->
                response.parseAs<List<WorkDto>>()
            }

            val orderedWorks = ids.mapNotNull { id -> works.firstOrNull { it.id() == id } }
            val offset = (page - 1) * PAGE_SIZE
            val pageItems = orderedWorks.drop(offset).take(PAGE_SIZE)

            MangasPage(
                pageItems.map { it.toSManga() },
                orderedWorks.size > offset + PAGE_SIZE,
            )
        }
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val sort = filters.sortValue().ifEmpty { LATEST_SORT }
        return worksRequest(page, sort, filters)
    }

    override fun searchMangaParse(response: Response): MangasPage = response.toWorksPage()

    // ============================= Details ================================

    override fun mangaDetailsRequest(manga: SManga): Request {
        val url = "$apiUrl/rest/v1/obras".toHttpUrl().newBuilder()
            .addQueryParameter("select", DETAILS_SELECT)
            .addQueryParameter("id", "eq.${manga.workId()}")
            .build()

        return GET(url, restHeaders)
    }

    override fun mangaDetailsParse(response: Response): SManga = response.parseAs<List<WorkDto>>().first().toSManga(initialized = true)

    // ============================= Chapters ===============================

    override fun chapterListRequest(manga: SManga): Request {
        val url = "$apiUrl/rest/v1/capitulos".toHttpUrl().newBuilder()
            .addQueryParameter("select", CHAPTERS_SELECT)
            .addQueryParameter("obra_id", "eq.${manga.workId()}")
            .addQueryParameter("order", "numero.asc")
            .build()

        return GET(url, restHeaders)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val workId = response.request.url.queryParameter("obra_id")?.removePrefix("eq.").orEmpty()
        return response.parseAs<List<ChapterDto>>()
            .map { it.toSChapter(workId) }
            .sortedWith(compareByDescending<SChapter> { it.chapter_number }.thenByDescending { it.date_upload })
    }

    // ============================= Pages ==================================

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> = Observable.fromCallable {
        val chapterUrl = "$baseUrl${chapter.url}".toHttpUrl()
        val workId = chapterUrl.pathSegments[1]
        val chapterId = chapterUrl.queryParameter("id") ?: throw IOException("ID do capitulo ausente")

        val folder = client.newCall(folderRequest(workId)).execute().use { response ->
            response.parseAs<List<FolderDto>>().first()
        }

        client.newCall(chapterPagesRequest(chapterId)).execute().use { response ->
            response.parseAs<ChapterPagesDto>().toPageList(folder)
        }
    }

    override fun pageListRequest(chapter: SChapter): Request = throw UnsupportedOperationException()

    override fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException()

    override fun imageRequest(page: Page): Request {
        val imageUrl = page.imageUrl ?: throw IOException("URL da imagem ausente")
        return GET(imageUrl, imageHeaders)
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // ============================= Filters ================================

    override fun getFilterList(): FilterList = getFilters()

    // ============================= Utils ==================================

    override fun getMangaUrl(manga: SManga): String = "$baseUrl${manga.url}"

    override fun getChapterUrl(chapter: SChapter): String = "$baseUrl${chapter.url.substringBefore("?")}"

    private fun worksRequest(page: Int, sort: String, filters: FilterList): Request {
        val url = "$apiUrl/rest/v1/obras".toHttpUrl().newBuilder()
            .addQueryParameter("select", CATALOG_SELECT)
            .addQueryParameter("or", "(is_acervo_b.is.null,is_acervo_b.eq.false)")
            .addQueryParameter("order", sort)
            .addQueryParameter("offset", ((page - 1) * PAGE_SIZE).toString())
            .addQueryParameter("limit", PAGE_SIZE.toString())

        url.applyFilters(filters)

        return GET(url.build(), restHeaders)
    }

    private fun worksByIdsRequest(ids: List<String>, filters: FilterList): Request {
        val url = "$apiUrl/rest/v1/obras".toHttpUrl().newBuilder()
            .addQueryParameter("select", CATALOG_SELECT)
            .addQueryParameter("id", "in.(${ids.joinToString()})")
            .addQueryParameter("or", "(is_acervo_b.is.null,is_acervo_b.eq.false)")

        url.applyFilters(filters)

        return GET(url.build(), restHeaders)
    }

    private fun searchIdsRequest(query: String): Request {
        val payload = SearchRequestDto(
            searchTerm = query.trim(),
            maxResults = SEARCH_LIMIT,
            archiveBOnly = false,
        ).toJsonRequestBody()

        return POST("$apiUrl/rest/v1/rpc/fuzzy_search_obras", rpcHeaders, payload)
    }

    private fun folderRequest(workId: String): Request {
        val url = "$apiUrl/rest/v1/obras".toHttpUrl().newBuilder()
            .addQueryParameter("select", FOLDER_SELECT)
            .addQueryParameter("id", "eq.$workId")
            .build()

        return GET(url, restHeaders)
    }

    private fun chapterPagesRequest(chapterId: String): Request {
        val payload = ChapterRequestDto(chapterId).toJsonRequestBody()
        return POST("$apiUrl/functions/v1/get-chapter", functionHeaders, payload)
    }

    private fun HttpUrl.Builder.applyFilters(filters: FilterList) {
        filters.formatValue().takeIf { it.isNotEmpty() }?.let {
            addQueryParameter("formato", "in.($it)")
        }

        filters.statusValue().takeIf { it.isNotEmpty() }?.let {
            addQueryParameter("status", "eq.$it")
        }

        filters.chapterRange()?.let { (min, max) ->
            min?.let { addQueryParameter("total_capitulos", "gte.$it") }
            max?.let { addQueryParameter("total_capitulos", "lte.$it") }
        }

        filters.selectedTags().takeIf { it.isNotEmpty() }?.let { tags ->
            val selected = tags.joinToString(prefix = "{", postfix = "}")
            addQueryParameter("or", "(tags.cs.$selected,generos.cs.$selected)")
        }
    }

    private fun Response.toWorksPage(): MangasPage {
        val page = request.url.queryParameter("offset")?.toIntOrNull()?.div(PAGE_SIZE)?.plus(1) ?: 1
        val works = parseAs<List<WorkDto>>()
        val total = header("content-range")?.substringAfter("/")?.toIntOrNull()
        val hasNextPage = total?.let { page * PAGE_SIZE < it } ?: (works.size == PAGE_SIZE)

        return MangasPage(works.map { it.toSManga() }, hasNextPage)
    }

    private fun SManga.workId(): String = "$baseUrl$url".toHttpUrl().pathSegments[1]

    private fun apiHeadersBuilder(): Headers.Builder = headersBuilder()
        .set("apikey", API_KEY)
        .set("Authorization", "Bearer $API_KEY")
        .set("x-client-info", CLIENT_INFO)

    private val imageHeaders: Headers by lazy {
        headersBuilder()
            .set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .set("Referer", "$baseUrl/")
            .build()
    }

    companion object {
        private const val PAGE_SIZE = 24
        private const val SEARCH_LIMIT = 120
        private const val POPULAR_SORT = "total_views.desc"
        private const val LATEST_SORT = "created_at.desc"
        private const val CLIENT_INFO = "supabase-js-web/2.99.3"
        private const val API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNqeWJmdnlvem5tdHhtamh5Y29qIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njk1NTI3MTIsImV4cCI6MjA4NTEyODcxMn0." +
                "0nWTir-WVr83QrPoIj8GbSt2Tuu3QZONA_TMzyZ8Ljc"
        private const val CATALOG_SELECT =
            "id,titulo,capa_url,tipo,formato,status,generos,tags,total_views,total_curtidas,total_capitulos,updated_at,created_at"
        private const val DETAILS_SELECT =
            "id,titulo,capa_url,descricao,status,tipo,formato,generos,tags,autor,artista,titulos_alternativos,updated_at,created_at,is_acervo_b"
        private const val CHAPTERS_SELECT = "id,numero,titulo,created_at"
        private const val FOLDER_SELECT = "id,titulo,capa_url,tipo,slug,pasta_s3,is_acervo_b"
    }
}
