package eu.kanade.tachiyomi.extension.pt.mediocretoons

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.parseAs
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MediocreToons : HttpSource() {

    override val name = "Mediocre Toons"

    override val baseUrl = "https://mediocrescan.com"

    override val lang = "pt-BR"

    override val supportsLatest = true

    private val apiUrl = "https://api.mediocretoons.site"

    private var authToken: String? = null

    override val client = network.cloudflareClient.newBuilder()
        .rateLimit(2)
        .addInterceptor(::authIntercept)
        .build()

    private fun authIntercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val token = authToken ?: fetchAuthToken()

        if (token.isNullOrEmpty()) {
            throw IOException("Faça login na WebView")
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authenticatedRequest)

        if (response.code in listOf(400, 401, 403)) {
            response.close()
            authToken = null
            throw IOException("Vip não ativo")
        }

        return response
    }

    private fun fetchAuthToken(): String? {
        authToken?.let { return it }

        val loginBody = """{"email":"gfuzetti67@gmail.com","senha":"236593nox"}"""
            .toRequestBody("application/json".toMediaType())

        val loginRequest = POST(
            "$apiUrl/usuarios/login",
            headers,
            loginBody,
        )

        val response = network.cloudflareClient
            .newCall(loginRequest)
            .execute()

        if (!response.isSuccessful) {
            response.close()
            return null
        }

        val loginResponse = response.parseAs<LoginResponseDto>()
        authToken = loginResponse.token
        return authToken
    }

    @Serializable
    private data class LoginResponseDto(
        val message: String,
        val statusCode: Int,
        val token: String,
    )

    override fun headersBuilder() = super.headersBuilder()
        .set("x-app-key", APP_KEY)
        .set("Referer", "$baseUrl/")
        .set("Origin", baseUrl)

    // ============================== Popular ================================
    override fun popularMangaRequest(page: Int): Request {
        val url = "$apiUrl/obras".toHttpUrl().newBuilder()
            .addQueryParameter("pagina", page.toString())
            .addQueryParameter("limite", PAGE_SIZE.toString())
            .addQueryParameter("temCapitulo", "true")
            .addQueryParameter("ordenarPor", "view_geral")
            .build()
        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val dto = response.parseAs<MediocreListDto<List<MediocreMangaDto>>>()
        val mangas = dto.data.map { it.toSManga() }
        val hasNext = dto.pagination?.hasNextPage ?: false
        return MangasPage(mangas, hasNextPage = hasNext)
    }

    // ============================= Latest Updates ==========================
    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$apiUrl/obras".toHttpUrl().newBuilder()
            .addQueryParameter("pagina", page.toString())
            .addQueryParameter("limite", PAGE_SIZE.toString())
            .addQueryParameter("temCapitulo", "true")
            .addQueryParameter("ordenarPor", "criada_em_desc")
            .build()
        return GET(url, headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        return popularMangaParse(response)
    }

    // =============================== Search ================================
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$apiUrl/obras".toHttpUrl().newBuilder()
            .addQueryParameter("limite", PAGE_SIZE.toString())
            .addQueryParameter("pagina", page.toString())
            .addQueryParameter("temCapitulo", "true")

        if (query.isNotEmpty()) {
            url.addQueryParameter("string", query)
        }

        filters.forEach { filter ->
            when (filter) {
                is FormatFilter -> {
                    if (filter.selected.isNotEmpty()) {
                        url.addQueryParameter("formato", filter.selected)
                    }
                }
                is StatusFilter -> {
                    if (filter.selected.isNotEmpty()) {
                        url.addQueryParameter("status", filter.selected)
                    }
                }
                is SortFilter -> {
                    url.addQueryParameter("ordenarPor", filter.selected)
                }
                else -> {}
            }
        }

        return GET(url.build(), headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        return popularMangaParse(response)
    }

    // ============================== Filters ================================
    override fun getFilterList() = eu.kanade.tachiyomi.extension.pt.mediocretoons.getFilterList()

    // ============================ Manga Details ============================
    override fun getMangaUrl(manga: SManga): String {
        val id = manga.url.substringAfter("/obra/")
        return "$baseUrl/obra/$id"
    }

    override fun mangaDetailsRequest(manga: SManga): Request {
        val id = manga.url.substringAfter("/obra/")
        return GET("$apiUrl/obras/$id", headers)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val dto = response.parseAs<MediocreMangaDto>()
        return dto.toSManga(isDetails = true)
    }

    // ============================== Chapters ===============================
    override fun getChapterUrl(chapter: SChapter): String {
        val id = chapter.url.substringAfter("/capitulo/")
        return "$baseUrl/capitulo/$id"
    }

    override fun chapterListRequest(manga: SManga): Request {
        return mangaDetailsRequest(manga)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val manga = response.parseAs<MediocreMangaDto>()

        return manga.chapters
            .map { it.toSChapter() }
            .distinctBy { it.url }
            .sortedByDescending { it.chapter_number }
    }

    // =============================== Pages =================================
    override fun pageListRequest(chapter: SChapter): Request {
        val chapterId = chapter.url.substringAfterLast("/")
        return GET("$apiUrl/capitulos/$chapterId", headers)
    }

    override fun pageListParse(response: Response): List<Page> {
        val dto = response.parseAs<MediocreChapterDetailDto>()
        return dto.toPageList()
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException()
    }

    override fun imageUrlRequest(page: Page): Request {
        val imageHeaders = headers.newBuilder()
            .add("Referer", "$baseUrl/")
            .build()
        return GET(page.url, imageHeaders)
    }

    companion object {
        const val CDN_URL = "https://cdn.mediocretoons.site"

        private const val APP_KEY = "toons-mediocre-app"
        private const val PAGE_SIZE = 20
    }
}
