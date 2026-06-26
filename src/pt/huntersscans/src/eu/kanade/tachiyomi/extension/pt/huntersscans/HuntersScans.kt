package eu.kanade.tachiyomi.extension.pt.huntersscans

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.network.rateLimit
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.jsoup.nodes.Document
import rx.Observable
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

class HuntersScans :
    Madara(
        "Hunters Scan",
        "https://readhunters.xyz",
        "pt-BR",
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
    ) {
    override val client = super.client.newBuilder()
        .readTimeout(1.minutes)
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.request.url.pathSegments.any { segment -> listOf("logar", "registrar").any { it.equals(segment, true) } }) {
                response.close()
                throw IOException("Faça o login na WebView")
            }
            response
        }
        .addInterceptor(::imageInterceptor)
        .rateLimit(2)
        .build()

    override fun headersBuilder() = super.headersBuilder()
        .add("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5")
        .add("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")

    override val mangaSubString = "comics"

    override val useLoadMoreRequest = LoadMoreStrategy.Always

    override val mangaDetailsSelectorStatus = "div.summary-heading:contains(Status) + div"

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> = Observable.fromCallable { fetchAllChapters(manga) }

    private fun fetchAllChapters(manga: SManga): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        var page = 1
        while (true) {
            val document = client.newCall(POST("${getMangaUrl(manga)}ajax/chapters?t=${page++}", xhrHeaders))
                .execute()
                .asJsoup()
            val currentPage = document.select(chapterListSelector())
                .map(::chapterFromElement)

            chapters += currentPage

            if (currentPage.isEmpty()) {
                return chapters
            }
        }
    }

    override fun pageListParse(document: Document): List<Page> {
        val script = document.selectFirst("script:containsData(_HuntersOpts)")?.data()

        if (script != null) {
            val payload = PAYLOAD_REGEX.find(script)?.groupValues?.get(1)
            val sk = SK_REGEX.find(script)?.groupValues?.get(1)

            if (!payload.isNullOrEmpty() && sk != null) {
                try {
                    val urls = HuntersScanDescrambler.decryptHuntersPayload(payload, sk)
                    return urls.mapIndexed { index, url -> Page(index, document.location(), url) }
                } catch (e: Exception) {
                }
            }
        }

        return document.select("div.reading-content img").mapNotNull { img ->
            val url = img.attr("abs:src").ifEmpty { img.attr("abs:data-src") }
            if (url.isEmpty()) return@mapNotNull null

            val isBanner = url.contains("discord", true) ||
                url.contains("vip", true) ||
                img.hasClass("aligncenter") ||
                img.hasClass("img-responsive") ||
                img.attr("alt").contains("Discord", true) ||
                img.attr("alt").contains("VIP", true) ||
                url.contains("999.png") ||
                url.contains("bla2-teste")

            if (isBanner) return@mapNotNull null
            url
        }.mapIndexed { index, url -> Page(index, document.location(), url) }
    }

    private fun imageInterceptor(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (request.url.toString().contains("scrambler.php")) {
            val scrambleKeyHeader = response.header("X-Scramble-Key")
            if (scrambleKeyHeader != null) {
                val imageStream = HuntersScanDescrambler.unscrambleImage(response.body.byteStream(), scrambleKeyHeader)
                val body = imageStream.readBytes().toResponseBody("image/jpeg".toMediaType())
                return response.newBuilder()
                    .body(body)
                    .build()
            }
        }

        return response
    }

    companion object {
        private val PAYLOAD_REGEX = Regex("""payload:\s*"(.*?)"""")
        private val SK_REGEX = Regex("""sk:\s*"(.*?)"""")
    }
}
