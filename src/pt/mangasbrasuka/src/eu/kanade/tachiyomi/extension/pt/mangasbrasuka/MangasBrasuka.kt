package eu.kanade.tachiyomi.extension.pt.mangasbrasuka

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.network.rateLimit
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class MangasBrasuka :
    Madara(
        "Mangas Brasuka",
        "https://mangasbrasuka.com.br",
        "pt-BR",
        SimpleDateFormat("MM/dd/yyyy", Locale.ROOT),
    ) {
    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val useNewChapterEndpoint = true

    override fun pageListParse(document: Document): List<Page> {
        val redirectUrls = document.select("div.page-break a[href], .page-link-wrap a[href]")
            .map { it.absUrl("href") }
            .filter(String::isNotBlank)
            .distinct()

        val imageUrls = redirectUrls.mapNotNull(::imageUrlFromRedirect)
        if (imageUrls.isEmpty()) {
            throw IOException("Nenhuma imagem encontrada")
        }

        return fetchCampaignPages(imageUrls.first())
    }

    private fun imageUrlFromRedirect(redirectUrl: String): String? = redirectUrl.toHttpUrlOrNull()
        ?.let { it.queryParameter("a") ?: it.queryParameter("t") }
        ?.takeIf(String::isNotBlank)

    private fun fetchCampaignPages(authUrl: String): List<Page> {
        val url = "$baseUrl/campanha.php".toHttpUrl().newBuilder()
            .addQueryParameter("auth", authUrl)
            .build()

        val pages = client.newCall(GET(url, headers)).execute().use { response ->
            response.asJsoup()
                .select(".manga-content img[src]")
                .mapIndexed { index, element ->
                    Page(index, imageUrl = element.absUrl("src"))
                }
        }

        if (pages.isEmpty()) {
            throw IOException("Nenhuma imagem encontrada")
        }

        return pages
    }
}
