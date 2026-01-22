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
import org.jsoup.nodes.Document
import java.io.IOException

class SakuraMangas : HttpSource() {
    override val lang = "pt-BR"
    override val supportsLatest = true
    override val name = "Sakura Mangás"
    override val baseUrl = "https://sakuramangas.org"

    private companion object {
        // Using Android Chrome UA to match WebView TLS fingerprint better
        const val CHROME_UA = "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.39 Mobile Safari/537.36"
        const val SEC_CH_UA = "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not A(Brand\";v=\"24\""
    }

    private val apiClient = network.client.newBuilder()
        .rateLimit(2)
        .build()

    override val client = network.cloudflareClient.newBuilder()
        .rateLimit(2)
        .build()

    private val webViewInterceptor by lazy { WebViewInterceptor(CHROME_UA, client, baseUrl) }

    // Mutex to serialize proof-dependent operations (manga details + chapter list)
    private val proofOperationsMutex = java.util.concurrent.Semaphore(1)

    // Cache for security scripts (they have version in filename, so URL is the key)
    private data class CachedScript(val content: String, val timestamp: Long)
    private val scriptCache = mutableMapOf<String, CachedScript>()
    private val scriptCacheTtl = 10 * 60 * 1000L // 10 minutes

    private fun getCachedScript(url: String): String? {
        val cached = scriptCache[url] ?: return null
        val age = System.currentTimeMillis() - cached.timestamp
        return if (age < scriptCacheTtl) cached.content else null
    }

    private fun cacheScript(url: String, content: String) {
        scriptCache[url] = CachedScript(content, System.currentTimeMillis())
    }

    private var genresSet: Set<Genre> = emptySet()
    private var demographyOptions = listOf("Todos" to "")
    private var classificationOptions = listOf("Todos" to "")
    private var orderByOptions = listOf("Lidos" to "3")
    private var fetchFiltersAttempts = 0
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun launchIO(block: () -> Unit) = scope.launch { block() }

    // ================================ Headers =======================================

    private fun baseHeaders() = Headers.Builder()
        .set("User-Agent", CHROME_UA)
        .set("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
        .set("Sec-CH-UA", SEC_CH_UA)
        .set("Sec-CH-UA-Mobile", "?1")
        .set("Sec-CH-UA-Platform", "\"Android\"")
        .set("DNT", "1")

    override fun headersBuilder() = baseHeaders()
        .set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
        .set("Sec-Fetch-Dest", "document")
        .set("Sec-Fetch-Mode", "navigate")
        .set("Sec-Fetch-Site", "none")
        .set("Sec-Fetch-User", "?1")
        .set("Upgrade-Insecure-Requests", "1")

    private fun ajaxHeaders(referer: String) = baseHeaders()
        .set("Accept", "text/html, */*; q=0.01")
        .set("Origin", baseUrl)
        .set("Referer", referer)
        .set("X-Requested-With", "XMLHttpRequest")
        .set("Sec-Fetch-Dest", "empty")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    private fun securityHeaders(keys: SecurityKeys, referer: String, token: String) = ajaxHeaders(referer).newBuilder()
        .add("X-Client-Signature", keys.clientSignature)
        .add("X-Verification-Key-1", keys.xVerificationKey1)
        .add("X-Verification-Key-2", keys.xVerificationKey2)
        .add("X-CSRF-Token", token)
        .build()

    private fun scriptHeaders() = baseHeaders()
        .set("Accept", "*/*")
        .set("Referer", "$baseUrl/")
        .set("Sec-Fetch-Dest", "script")
        .set("Sec-Fetch-Mode", "no-cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    private fun imageHeaders() = baseHeaders()
        .set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*;q=0.8,*/*;q=0.5")
        .set("Referer", "$baseUrl/")
        .set("Sec-Fetch-Dest", "image")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .set("X-Requested-With", "SakuraMatchClient")
        .set("X-Signature-Version", "v5-fetch-custom")
        .build()

    // ================================ Requests =======================================

    override fun mangaDetailsRequest(manga: SManga) = GET(baseUrl + manga.url, headers)
    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)
    override fun pageListRequest(chapter: SChapter) = GET(baseUrl + chapter.url, headers)
    override fun imageRequest(page: Page) = GET(page.imageUrl!!, imageHeaders())
    override fun getMangaUrl(manga: SManga) = "$baseUrl${manga.url}"

    // ================================ Popular =======================================

    override fun popularMangaRequest(page: Int) = GET("$baseUrl/dist/sakura/models/home/__.home_maislidos.php", headers)

    override fun fetchPopularManga(page: Int) = apiClient.newCall(popularMangaRequest(page))
        .asObservableSuccess()
        .map { popularMangaParse(it) }

    override fun popularMangaParse(response: Response): MangasPage {
        val mangas = response.parseAs<SakuraMangasPopularResponseDto>().data.map { it.toSManga(baseUrl) }
        return MangasPage(mangas, hasNextPage = false)
    }

    // ================================ Latest =======================================

    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/dist/sakura/models/home/__.home_ultimos.php", headers)

    override fun fetchLatestUpdates(page: Int) = apiClient.newCall(latestUpdatesRequest(page))
        .asObservableSuccess()
        .map { latestUpdatesParse(it) }

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
        return POST("$baseUrl/dist/sakura/models/obras/__.obras__buscar.php", headers, form.build())
    }

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList) = apiClient.newCall(searchMangaRequest(page, query, filters))
        .asObservableSuccess()
        .map { searchMangaParse(it) }

    override fun searchMangaParse(response: Response): MangasPage {
        val result = response.parseAs<SakuraMangasSearchResponseDto>()
        return MangasPage(result.data.map { it.toSManga(baseUrl) }, result.hasMore)
    }

    // ================================ Details =======================================

    private class CloudflareRequiredException : IOException(
        "Cloudflare não resolvido. Abra no WebView.",
    )

    private fun validateNotBlocked(document: Document, url: String) {
        if (url.contains("block.php") || document.selectFirst("meta[manga-id]") == null) {
            throw IOException("Bloqueado pelo site. Abra a WebView para resolver.")
        }
    }

    private data class PageMetadata(val mangaId: String, val token: String)

    private fun extractMetadata(document: Document): PageMetadata {
        val mangaId = document.selectFirst("meta[manga-id]")!!.attr("manga-id")
        val token = document.selectFirst("meta[name=csrf-token]")!!.attr("content")
        return PageMetadata(mangaId, token)
    }

    private fun fetchSecurityKeys(document: Document): SecurityKeys {
        val securityScriptUrl = document
            .selectFirst("script[src*=manga], script[src*=capitulo]")
            ?.attr("abs:src")
            ?: throw IOException("Could not locate the security script")

        val normalizeScriptUrl = document
            .selectFirst("script[src*=normalize]")
            ?.attr("abs:src")
            ?: throw IOException("Could not locate the normalize script")

        val securityScript = getCachedScript(securityScriptUrl)
            ?: client.newCall(GET(securityScriptUrl, scriptHeaders())).execute().body.string()
                .also { cacheScript(securityScriptUrl, it) }

        val normalizeScript = getCachedScript(normalizeScriptUrl)
            ?: client.newCall(GET(normalizeScriptUrl, scriptHeaders())).execute().body.string()
                .also { cacheScript(normalizeScriptUrl, it) }

        return SecurityKeysExtractor.extract(securityScript, normalizeScript)
    }

    override fun fetchMangaDetails(manga: SManga) = client.newCall(mangaDetailsRequest(manga))
        .asObservableSuccess()
        .map { mangaDetailsParse(it) }

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()
        val url = response.request.url.toString()

        validateNotBlocked(document, url)

        val (mangaId, token) = extractMetadata(document)
        val keys = fetchSecurityKeys(document)

        proofOperationsMutex.acquire()
        try {
            val proofResult = webViewInterceptor.getProof(url)
                ?: throw CloudflareRequiredException()

            val form = FormBody.Builder()
                .add("manga_id", mangaId)
                .add("dataType", "json")
                .add("challenge", proofResult.challenge)
                .add("proof", proofResult.proof)
                .build()

            return client.newCall(POST("$baseUrl/dist/sakura/models/manga/.__obf__manga_info.php", securityHeaders(keys, url, token), form))
                .execute()
                .parseAs<SakuraMangaInfoDto>()
                .toSManga(document.baseUri())
        } finally {
            proofOperationsMutex.release()
        }
    }

    // ================================ Chapters =======================================

    override fun fetchChapterList(manga: SManga) = client.newCall(chapterListRequest(manga))
        .asObservableSuccess()
        .map { chapterListParse(it) }

    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()
        val url = response.request.url.toString()

        validateNotBlocked(document, url)

        val (mangaId, token) = extractMetadata(document)
        val keys = fetchSecurityKeys(document)

        proofOperationsMutex.acquire()
        try {
            val proofResult = webViewInterceptor.getProof(url)
                ?: throw CloudflareRequiredException()

            var page = 1
            val chapters = mutableListOf<SChapter>()
            var result: SakuraMangasChaptersDto

            do {
                val form = FormBody.Builder()
                    .add("manga_id", mangaId)
                    .add("offset", ((page - 1) * 90).toString())
                    .add("order", "desc")
                    .add("limit", "90")
                    .add("challenge", proofResult.challenge)
                    .add("proof", proofResult.proof)
                    .build()

                val chapterResponse = client
                    .newCall(POST("$baseUrl/dist/sakura/models/manga/.__obf__manga_capitulos.php", securityHeaders(keys, url, token), form))
                    .execute()

                if (!chapterResponse.isSuccessful) {
                    val errorBody = chapterResponse.body.string()
                    if (errorBody.contains("cf_clearance") || errorBody.contains("challenge-platform")) {
                        throw IOException("Cloudflare bloqueou. Abra a WebView para resolver.")
                    }
                    throw IOException("Erro ao buscar capítulos: HTTP ${chapterResponse.code}")
                }

                result = chapterResponse.parseAs()
                chapters += result.data.map { it.toSChapter() }
                page++
            } while (result.has_more)

            return chapters
        } finally {
            proofOperationsMutex.release()
        }
    }

    // ================================ Pages =======================================

    override fun pageListParse(response: Response): List<Page> {
        val url = response.request.url.toString()

        val chapterData = webViewInterceptor.getChapterData(url)
            ?: throw CloudflareRequiredException()

        return (1..chapterData.numPages).map { pageNum ->
            val imageUrl = "${chapterData.imageBaseUrl}${pageNum.toString().padStart(3, '0')}.${chapterData.imageExtension}"
            Page(pageNum - 1, imageUrl = imageUrl)
        }
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

            val genres = document.select("#generos-badges .genre-chip").map { el ->
                Genre(el.text(), el.attr("data-value"))
            }
            val themes = document.select("#temas-badges .genre-chip").map { el ->
                Genre(el.text(), el.attr("data-value"))
            }
            genresSet = (genres + themes).toSet()

            val demoOpts = document.select("#group-demografia .btn-filter-chip").mapNotNull { el ->
                val value = el.attr("data-value")
                val text = el.text()
                if (text.isEmpty()) null else text to value
            }
            if (demoOpts.isNotEmpty()) demographyOptions = demoOpts

            val classOpts = document.select("#group-classificacao .btn-filter-chip").mapNotNull { el ->
                val value = el.attr("data-value")
                val text = el.text()
                if (text.isEmpty()) null else text to value
            }
            if (classOpts.isNotEmpty()) classificationOptions = classOpts

            val orderOpts = document.select("#group-ordenacao .btn-sort-option").mapNotNull { el ->
                val value = el.attr("data-value")
                val text = el.text()
                if (text.isEmpty()) null else text to value
            }
            if (orderOpts.isNotEmpty()) orderByOptions = orderOpts
        }
    }
}
