package eu.kanade.tachiyomi.extension.pt.fenixproject

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushi.network.rateLimit
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale

class FenixProject :
    Madara(
        "Fenix Project",
        "https://fenixproject.site",
        "pt-BR",
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
    ) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val useNewChapterEndpoint = true

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override fun pageListRequest(chapter: SChapter): Request {
        val chapterUrl = if (chapter.url.startsWith("http")) {
            chapter.url
        } else {
            "$baseUrl/${chapter.url.removePrefix("/")}"
        }.toHttpUrl().newBuilder()
            .setQueryParameter("LSCWP_CTRL", "before_optm")
            .build()

        return GET(chapterUrl, headers)
    }
}
