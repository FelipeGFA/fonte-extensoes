package eu.kanade.tachiyomi.extension.pt.egotoons

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.network.rateLimit
import keiyoushi.utils.parseAs
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response

class EgoToons : HttpSource() {

    override val name = "Ego Toons"

    override val baseUrl = "https://egotoons.com"

    override val lang = "pt-BR"

    override val supportsLatest = true

    override val versionId = 3

    override val client = network.client.newBuilder()
        .addInterceptor(ApiDecryptInterceptor())
        .rateLimit(2)
        .build()

    override fun headersBuilder() = super.headersBuilder()
        .set("Referer", "$baseUrl/")
        .set("X-API-Token", "fqegqweg9u23wi4go32wh4gij")

    private val apiUrl = "https://api.egotoons.com/api/obras"

    // ============================== Popular ================================
    override fun popularMangaRequest(page: Int): Request {
        val url = "$apiUrl/top10/views".toHttpUrl().newBuilder()
            .addQueryParameter("periodo", "total")
            .build()
        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val result = response.parseAs<EgoToonsResponseDto<EgoToonsMangaDto>>()
        val mangas = result.obras.map { it.toSManga() }
        return MangasPage(mangas, false)
    }

    // ============================= Latest Updates ==========================
    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$apiUrl/recentes".toHttpUrl().newBuilder()
            .addQueryParameter("pagina", page.toString())
            .addQueryParameter("limite", "18")
            .build()
        return GET(url, headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        val result = response.parseAs<EgoToonsResponseDto<EgoToonsMangaDto>>()
        val mangas = result.obras.map { it.toSManga() }
        val hasNextPage = result.pagination?.hasNextPage ?: false
        return MangasPage(mangas, hasNextPage)
    }

    // =============================== Search ================================
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = apiUrl.toHttpUrl().newBuilder()
            .addQueryParameter("pagina", page.toString())
            .addQueryParameter("limite", "20")
            .addQueryParameter("ordenarPor", "criada_em_desc")

        if (query.isNotBlank()) {
            url.addQueryParameter("busca", query)
        }

        val tagIds = mutableListOf<String>()

        filters.forEach { filter ->
            when (filter) {
                is FormatFilter -> {
                    val id = filter.toUriPart()
                    if (id.isNotEmpty()) url.addQueryParameter("formato_id", id)
                }

                is StatusFilter -> {
                    val id = filter.toUriPart()
                    if (id.isNotEmpty()) url.addQueryParameter("status_id", id)
                }

                is TagFilter -> {
                    filter.state.filter { it.state }.forEach {
                        tagIds.add(it.id)
                    }
                }

                else -> {}
            }
        }

        if (tagIds.isNotEmpty()) {
            url.addQueryParameter("tag_ids", tagIds.joinToString(","))
        }

        return GET(url.build(), headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = latestUpdatesParse(response)

    // ============================== Filters ================================
    override fun getFilterList() = FilterList(
        FormatFilter(),
        StatusFilter(),
        TagFilter(),
    )

    // ============================ Manga Details ============================
    override fun mangaDetailsRequest(manga: SManga): Request {
        val url = apiUrl.toHttpUrl().newBuilder()
            .addPathSegment(getMangaId(manga.url))
            .build()

        return GET(url, headers)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val result = response.parseAs<EgoToonsResponseDto<EgoToonsMangaDto>>()
        val mangaDto = result.obra ?: throw Exception("Obra não encontrada")
        return mangaDto.toSManga()
    }

    // ============================== Chapters ===============================
    override fun chapterListRequest(manga: SManga): Request = mangaDetailsRequest(manga)

    override fun chapterListParse(response: Response): List<SChapter> {
        val result = response.parseAs<EgoToonsResponseDto<EgoToonsMangaDto>>()
        val mangaDto = result.obra ?: throw Exception("Obra não encontrada")
        return mangaDto.capitulos.map { it.toSChapter() }.sortedByDescending { it.chapter_number }
    }

    // =============================== Pages =================================
    override fun pageListRequest(chapter: SChapter): Request {
        val mangaId = getMangaId(chapter.url)
        val number = chapter.url.substringAfterLast("/")

        val url = apiUrl.toHttpUrl().newBuilder()
            .addPathSegment(mangaId)
            .addPathSegment("capitulos")
            .addPathSegment(number)
            .build()

        return GET(url, headers)
    }

    override fun pageListParse(response: Response): List<Page> {
        val result = response.parseAs<EgoToonsResponseDto<Unit>>()
        val pages = result.capitulo?.paginas ?: emptyList()

        if (pages.isEmpty()) {
            throw Exception("Lista de páginas vazia. Tente abrir na WebView.")
        }

        return pages.mapIndexed { index, page ->
            Page(index, imageUrl = page.url)
        }
    }

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()

    // =============================== Utils =================================

    override fun getMangaUrl(manga: SManga): String {
        val id = getMangaId(manga.url)
        return baseUrl.toHttpUrl().newBuilder()
            .addPathSegment("obra")
            .addPathSegment(id)
            .build()
            .toString()
    }

    override fun getChapterUrl(chapter: SChapter): String {
        val mangaId = getMangaId(chapter.url)
        val chapterSlug = if (chapter.url.startsWith("http")) {
            chapter.url.toHttpUrl().pathSegments.last()
        } else {
            chapter.url.trimEnd('/').substringAfterLast('/')
        }

        return baseUrl.toHttpUrl().newBuilder()
            .addPathSegment("obra")
            .addPathSegment(mangaId)
            .addPathSegment("capitulo")
            .addPathSegment(chapterSlug)
            .build()
            .toString()
    }

    private fun getMangaId(url: String): String {
        val absoluteUrl = if (url.startsWith("http")) {
            url
        } else {
            "$baseUrl/${url.trimStart('/')}"
        }

        val segments = absoluteUrl.toHttpUrl().pathSegments
        val index = segments.indexOfFirst { it == "obra" || it == "manga" }

        return segments[index + 1]
    }
}
