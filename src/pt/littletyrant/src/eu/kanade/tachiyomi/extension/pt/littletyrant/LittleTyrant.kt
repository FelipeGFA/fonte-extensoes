package eu.kanade.tachiyomi.extension.pt.littletyrant

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.network.rateLimit
import keiyoushi.utils.parseAs
import okhttp3.Cookie
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class LittleTyrant :
    Madara(
        "Little Tyrant",
        "https://tiraninha.world",
        "pt-BR",
        dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale("pt", "BR")),
    ) {

    override val client: OkHttpClient = network.client.newBuilder()
        .addInterceptor { chain ->
            val request = chain.request()
            val readerKey = request.url.fragment
            if (readerKey == null || !request.url.encodedPath.endsWith("/image-loader.php")) {
                return@addInterceptor chain.proceed(request)
            }

            val cleanRequest = request.newBuilder()
                .url(request.url.newBuilder().fragment(null).build())
                .build()

            decoder.decodeImage(chain.proceed(cleanRequest), readerKey.readerKey())
        }
        .rateLimit(3, 1.seconds)
        .build()

    private val decoder by lazy { Decoder() }

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    // =============================== Popular =================================

    override fun popularMangaSelector() = "[id*=manga-item-]"
    override val popularMangaUrlSelector = ".card-title a"

    override fun popularMangaFromElement(element: Element) = SManga.create().apply {
        title = element.selectFirst("h3")!!.text()
        thumbnail_url = element.selectFirst("img")?.absUrl("src")
        setUrlWithoutDomain(element.selectFirst(popularMangaUrlSelector)!!.absUrl("href"))
    }

    // =============================== Details =================================

    override val mangaDetailsSelectorGenre = ".mc-genres-pills a"
    override val mangaDetailsSelectorDescription = ".mc-description-box"
    override val mangaDetailsSelectorAuthor = ".mc-meta-grid .attr-item:has(.attr-label:contains(AUTOR)) .attr-value"
    override val mangaDetailsSelectorArtist = ".mc-meta-grid .attr-item:has(.attr-label:contains(ARTISTA)) .attr-value"
    override val mangaDetailsSelectorStatus = ".mc-meta-grid .attr-item:has(.attr-label:contains(STATUS)) .attr-value"

    override fun mangaDetailsParse(document: Document): SManga = super.mangaDetailsParse(document).apply {
        author = author?.replace(COMMA_REGEX, ", ")?.takeUnless { it.contains("---") }
        artist = artist?.replace(COMMA_REGEX, ", ")?.takeUnless { it.contains("---") }
    }

    // =============================== Chapters =================================

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> = Observable.fromCallable {
        val document = client.newCall(mangaDetailsRequest(manga)).execute().use { response ->
            response.asJsoup()
        }
        val mangaId = document.selectFirst("a.wp-manga-action-button")!!.attr("data-post")
        val chapters = mutableListOf<SChapter>()
        val url = "$baseUrl/wp-admin/admin-ajax.php"
        var offset = 0
        do {
            val form = FormBody.Builder()
                .add("action", "load_more_chapters")
                .add("manga_id", mangaId)
                .add("offset", offset.toString())
                .build()
            offset += 12
            val dto = client.newCall(POST(url, headers, form)).execute().use { response ->
                response.parseAs<ChapterDto>()
            }
            val chapterElements = dto.toJsoup(baseUrl).select(chapterListSelector())
            chapters += chapterElements.map(::chapterFromElement)
        } while (!dto.isEmpty())

        chapters.sortedByDescending(SChapter::chapter_number)
    }

    override fun chapterFromElement(element: Element) = SChapter.create().apply {
        name = element.selectFirst("span.mc-chapter-title")!!.text()
        date_upload = parseChapterDate(element.selectFirst(".mc-chapter-date")?.text())
        // The source chapter list is out of order, so extract the number here for later sorting
        CHAPTER_NUMBER_REGEX.find(name)?.groupValues?.last()?.toFloatOrNull()?.let {
            chapter_number = it
        }
        setUrlWithoutDomain(element.selectFirst("a.mc-chapter-link")!!.absUrl("href"))
    }

    // =============================== Pages =================================

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> = client.newCall(pageListRequest(chapter))
        .asObservableSuccess()
        .map { response ->
            response.use {
                val doc = it.asJsoup()
                launchIO { countViews(doc) }

                val token = fetchReaderToken(it.request.url.toString())

                decoder.extractPaths(doc, baseUrl).mapIndexed { idx, url ->
                    Page(idx, url = it.request.url.toString(), imageUrl = url.withReaderToken(token))
                }
            }
        }

    override fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException()

    // =============================== Images =================================

    override fun imageRequest(page: Page): Request {
        val imageUrl = page.imageUrl!!.toHttpUrl()
        imageUrl.fragment?.let(::saveReaderCookie)
        return GET(imageUrl, readerHeaders(page.url))
    }

    private fun fetchReaderToken(referer: String): String {
        val url = "$baseUrl/wp-content/themes/madara2/gatekeeper.php".toHttpUrl().newBuilder()
            .addQueryParameter("t", System.currentTimeMillis().toString())
            .build()

        return client.newCall(GET(url, readerHeaders(referer))).execute().use { response ->
            response.parseAs<TokenDto>().token().also(::saveReaderCookie)
        }
    }

    private fun readerHeaders(referer: String): Headers = headers.newBuilder()
        .set("Accept", "*/*")
        .set("Referer", referer)
        .set("Origin", baseUrl)
        .set("X-Reader-Sec", READER_SEC)
        .set("Sec-Fetch-Dest", "empty")
        .set("Sec-Fetch-Mode", "cors")
        .set("Sec-Fetch-Site", "same-origin")
        .build()

    private fun saveReaderCookie(token: String) {
        val cookie = Cookie.Builder()
            .name(READER_COOKIE)
            .value(token)
            .domain(baseUrl.toHttpUrl().host)
            .path("/")
            .build()

        client.cookieJar.saveFromResponse(baseUrl.toHttpUrl(), listOf(cookie))
    }

    private fun String.readerKey(): String = substringAfter('.').substring(4, 20)

    private fun String.withReaderToken(token: String): String = toHttpUrl().newBuilder()
        .fragment(token)
        .build()
        .toString()

    companion object {
        private const val READER_SEC = "tiraninha-web"
        private const val READER_COOKIE = "lt_sec_val"
        private val CHAPTER_NUMBER_REGEX = """\d+(?:\.\d+)?""".toRegex()
        private val COMMA_REGEX = """,\s*""".toRegex()
    }
}
