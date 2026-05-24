package eu.kanade.tachiyomi.extension.pt.mugiwarasoficial

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import java.text.SimpleDateFormat
import java.util.Locale

class MugiwarasOficial :
    Madara(
        "Mugiwaras Oficial",
        "https://mugiwarasoficial.com",
        "pt-BR",
        SimpleDateFormat("d 'de' MMM 'de' yyyy", Locale("pt", "BR")),
    ) {

    override val client = super.client.newBuilder()
        .rateLimit(3, 1)
        .build()

    override val useNewChapterEndpoint = true

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val pageListParseSelector = "img.wp-manga-chapter-img"
}
