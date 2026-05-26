package eu.kanade.tachiyomi.extension.pt.hentaitokyo

import eu.kanade.tachiyomi.multisrc.gattsu.Gattsu
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit

class HentaiTokyo :
    Gattsu(
        "Hentai Tokyo",
        "https://hentaitokyo.net",
        "pt-BR",
    ) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()

    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()

        return listOf(
            SChapter.create().apply {
                name = "Capítulo único"
                scanlator = document.selectFirst("ul.post-itens li:contains(Tradutor) a")?.text()
                date_upload = document.selectFirst("meta[property=article:published_time]")
                    ?.attr("content")
                    .orEmpty()
                    .toDate()
                setUrlWithoutDomain(response.request.url.toString())
            },
        )
    }

    override fun pageListSelector(): String = "div.meio ul.post-fotos img, " +
        "div.meio div.post-box.listaImagens div.galeriaHtml img"
}
