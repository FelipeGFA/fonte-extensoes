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
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SakuraMangas : HttpSource() {
    override val lang = "pt-BR"
    override val supportsLatest = true
    override val name = "Sakura Mangás"
    override val baseUrl = "https://sakuramangas.org"

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.6778.39 Mobile Safari/537.36"
        const val SEC_CH_UA = "\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\""
        const val CHAPTERS_API = "/dist/sakura/models/manga/.__obf__manga_capitulos.php"
    }

    private val apiClient = network.cloudflareClient.newBuilder()
        .rateLimit(2)
        .build()

    override val client = network.cloudflareClient.newBuilder()
        .rateLimit(2)
        .build()

    private val webViewInterceptor by lazy { WebViewInterceptor(USER_AGENT, client, baseUrl) }

    @Volatile
    private var cachedSecurityKeys: SecurityKeys? = null

    @Volatile
    private var securityKeysTimestamp: Long = 0
    private val securityKeysTtl = 30 * 60 * 1000L

    private var genresSet: Set<Genre> = emptySet()
    private var demographyOptions = listOf("Todos" to "")
    private var classificationOptions = listOf("Todos" to "")
    private var orderByOptions = listOf("Lidos" to "3")
    private var fetchFiltersAttempts = 0
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun launchIO(block: () -> Unit) = scope.launch { block() }

    // ================================ Headers =======================================

    private fun baseHeaders() = Headers.Builder()
        .set("User-Agent", USER_AGENT)
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
        .set("Accept", "*/*")
        .set("Cache-Control", "no-cache")
        .set("Pragma", "no-cache")
        .set("Origin", baseUrl)
        .set("Referer", referer)
        .set("X-Requested-With", "XMLHttpRequest")
        .set("Sec-Fetch-Dest", "empty")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    private fun imageHeaders(xRequestedWith: String, xSignatureVersion: String) = baseHeaders()
        .set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*;q=0.8,*/*;q=0.5")
        .set("Referer", "$baseUrl/")
        .set("Sec-Fetch-Dest", "image")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .set("X-Requested-With", xRequestedWith)
        .set("X-Signature-Version", xSignatureVersion)
        .build()

    @Volatile
    private var cachedImageHeaders: Pair<String, String>? = null

    @Volatile
    private var imageHeadersTimestamp: Long = 0
    private val imageHeadersTtl = 30 * 60 * 1000L

    private fun securityHeaders(
        token: String,
        securityKeys: SecurityKeys?,
        referer: String,
        cookies: String,
    ) = ajaxHeaders(referer).newBuilder()
        .set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .add("X-Client-Signature", securityKeys?.clientSignature.orEmpty())
        .add("X-Verification-Key-1", securityKeys?.xVerificationKey1.orEmpty())
        .add("X-Verification-Key-2", securityKeys?.xVerificationKey2.orEmpty())
        .add("X-CSRF-Token", token)
        .add("Cookie", cookies)
        .build()

    // ================================ Security Keys =======================================

    private data class CachedScript(val content: String, val timestamp: Long)

    private val scriptCache = mutableMapOf<String, CachedScript>()
    private val scriptCacheTtl = 10 * 60 * 1000L

    private fun getCachedScript(url: String): String? {
        val cached = scriptCache[url] ?: return null
        val age = System.currentTimeMillis() - cached.timestamp
        return if (age < scriptCacheTtl) cached.content else null.also { scriptCache.remove(url) }
    }

    private fun cacheScript(url: String, content: String) {
        scriptCache[url] = CachedScript(content, System.currentTimeMillis())
    }

    private fun scriptHeaders() = baseHeaders()
        .set("Accept", "*/*")
        .set("Referer", "$baseUrl/")
        .set("Sec-Fetch-Dest", "script")
        .set("Sec-Fetch-Mode", "no-cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    private fun getSecurityKeys(pageUrl: String, cookies: String): SecurityKeys? {
        val now = System.currentTimeMillis()

        if (cachedSecurityKeys != null && (now - securityKeysTimestamp) < securityKeysTtl) {
            return cachedSecurityKeys
        }

        return try {
            val pageRequest = Request.Builder()
                .url(pageUrl)
                .headers(headers.newBuilder().set("Cookie", cookies).build())
                .get()
                .build()

            val document = client.newCall(pageRequest).execute().use { response ->
                if (!response.isSuccessful) return null
                response.asJsoup()
            }

            val securityScriptUrl = document
                .selectFirst("script[src*=manga], script[src*=capitulo]")
                ?.absUrl("src") ?: return null

            val normalizeScriptUrl = document
                .selectFirst("script[src*=normalize]")
                ?.absUrl("src")

            val securityScript = getCachedScript(securityScriptUrl)
                ?: client.newCall(GET(securityScriptUrl, scriptHeaders())).execute().use { resp ->
                    if (resp.isSuccessful) resp.body.string().also { cacheScript(securityScriptUrl, it) } else ""
                }

            val normalizeScript = normalizeScriptUrl?.let {
                getCachedScript(it)
                    ?: client.newCall(GET(it, scriptHeaders())).execute().use { resp ->
                        if (resp.isSuccessful) resp.body.string().also { content -> cacheScript(it, content) } else ""
                    }
            }.orEmpty()

            if (securityScript.isEmpty()) return null

            SecurityKeysExtractor.extract(securityScript, normalizeScript).also {
                cachedSecurityKeys = it
                securityKeysTimestamp = now
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getImageHeaders(chapterUrl: String, cookies: String): Pair<String, String>? {
        val now = System.currentTimeMillis()

        if (cachedImageHeaders != null && (now - imageHeadersTimestamp) < imageHeadersTtl) {
            return cachedImageHeaders
        }

        return try {
            val pageRequest = Request.Builder()
                .url(chapterUrl)
                .headers(headers.newBuilder().set("Cookie", cookies).build())
                .get()
                .build()

            val document = client.newCall(pageRequest).execute().use { response ->
                if (!response.isSuccessful) return null
                response.asJsoup()
            }

            val chapterScriptUrl = document
                .selectFirst("script[src*=capitulo][src*=core.js]")
                ?.absUrl("src") ?: return null

            val chapterScript = getCachedScript(chapterScriptUrl)
                ?: client.newCall(GET(chapterScriptUrl, scriptHeaders())).execute().use { resp ->
                    if (resp.isSuccessful) resp.body.string().also { cacheScript(chapterScriptUrl, it) } else ""
                }

            if (chapterScript.isEmpty()) return null

            val baseKeys = cachedSecurityKeys ?: SecurityKeys(0, 0, "", "", "", "", "")
            val keysWithImageHeaders = SecurityKeysExtractor.extractImageHeaders(chapterScript, baseKeys)

            (keysWithImageHeaders.imageXRequestedWith to keysWithImageHeaders.imageXSignatureVersion).also {
                cachedImageHeaders = it
                imageHeadersTimestamp = now
            }
        } catch (_: Exception) {
            null
        }
    }

    // ================================ Requests =======================================

    override fun mangaDetailsRequest(manga: SManga) = GET(baseUrl + manga.url, headers)
    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)
    override fun pageListRequest(chapter: SChapter) = GET(baseUrl + chapter.url, headers)

    override fun imageRequest(page: Page): Request {
        val (xRequestedWith, xSignatureVersion) = cachedImageHeaders ?: ("" to "")
        return GET(page.imageUrl!!, imageHeaders(xRequestedWith, xSignatureVersion))
    }

    override fun getMangaUrl(manga: SManga) = "$baseUrl${manga.url}"

    // ================================ Popular =======================================

    override fun popularMangaRequest(page: Int) = GET("$baseUrl/dist/sakura/models/home/__.home_maislidos.php", ajaxHeaders("$baseUrl/"))

    override fun fetchPopularManga(page: Int) = apiClient.newCall(popularMangaRequest(page))
        .asObservableSuccess()
        .map { popularMangaParse(it) }

    override fun popularMangaParse(response: Response): MangasPage {
        val mangas = response.parseAs<SakuraMangasPopularResponseDto>().data.map { it.toSManga(baseUrl) }
        return MangasPage(mangas, hasNextPage = false)
    }

    // ================================ Latest =======================================

    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/dist/sakura/models/home/__.home_ultimos.php", ajaxHeaders("$baseUrl/"))

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
        return POST("$baseUrl/dist/sakura/models/obras/__.obras__buscar.php", ajaxHeaders("$baseUrl/obras/"), form.build())
    }

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList) = apiClient.newCall(searchMangaRequest(page, query, filters))
        .asObservableSuccess()
        .map { searchMangaParse(it) }

    override fun searchMangaParse(response: Response): MangasPage {
        val result = response.parseAs<SakuraMangasSearchResponseDto>()
        return MangasPage(result.data.map { it.toSManga(baseUrl) }, result.hasMore)
    }

    // ================================ Details =======================================

    private class CloudflareRequiredException : IOException("Cloudflare não resolvido. Abra no WebView.")

    override fun fetchMangaDetails(manga: SManga) = client.newCall(mangaDetailsRequest(manga))
        .asObservableSuccess()
        .map { mangaDetailsParse(it) }

    override fun mangaDetailsParse(response: Response): SManga {
        val url = response.request.url.toString()
        response.close()

        val securityData = webViewInterceptor.captureSecurityData(url)
            ?: throw CloudflareRequiredException()

        val mangaInfoJson = securityData.mangaInfoJson
            ?: throw CloudflareRequiredException()

        return mangaInfoJson.parseAs<SakuraMangaInfoDto>().toSManga(url)
    }

    // ================================ Chapters =======================================

    override fun fetchChapterList(manga: SManga) = client.newCall(chapterListRequest(manga))
        .asObservableSuccess()
        .map { chapterListParse(it) }

    override fun chapterListParse(response: Response): List<SChapter> {
        val url = response.request.url.toString()
        response.close()

        val securityData = webViewInterceptor.captureSecurityData(url)
            ?: throw CloudflareRequiredException()

        if (securityData.proof.isEmpty() || securityData.challenge.isEmpty() || securityData.mangaId.isEmpty()) {
            throw CloudflareRequiredException()
        }

        val securityKeys = getSecurityKeys(url, securityData.cookies)
        val chapters = mutableListOf<SChapter>()

        var offset = 0
        var limit = 10
        var hasMore = true
        var pageCount = 0

        while (hasMore && pageCount < 100) {
            pageCount++

            val form = FormBody.Builder()
                .add("manga_id", securityData.mangaId)
                .add("offset", offset.toString())
                .add("order", "desc")
                .add("limit", limit.toString())
                .addEncoded("challenge", securityData.challenge)
                .add("proof", securityData.proof)
                .build()

            val request = Request.Builder()
                .url("$baseUrl$CHAPTERS_API")
                .headers(securityHeaders(securityData.csrfToken, securityKeys, url, securityData.cookies))
                .post(form)
                .build()

            val chaptersResponse = client.newCall(request).execute()

            if (!chaptersResponse.isSuccessful) break

            val result = try {
                chaptersResponse.body.string().parseAs<SakuraMangasChaptersDto>()
            } catch (_: Exception) {
                break
            }

            chapters += result.data.map { it.toSChapter() }
            hasMore = result.has_more

            if (!hasMore) break

            offset = if (offset == 0) 10 else offset + 90
            limit = 90
        }

        return chapters.distinctBy { it.url }
    }

    // ================================ Pages =======================================

    override fun pageListParse(response: Response): List<Page> {
        val url = response.request.url.toString()

        val chapterData = webViewInterceptor.getChapterData(url)
            ?: throw CloudflareRequiredException()

        if (cachedImageHeaders == null || (System.currentTimeMillis() - imageHeadersTimestamp) >= imageHeadersTtl) {
            val cookies = client.cookieJar.loadForRequest(url.toHttpUrl())
                .joinToString("; ") { "${it.name}=${it.value}" }
            getImageHeaders(url, cookies)
        }

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
