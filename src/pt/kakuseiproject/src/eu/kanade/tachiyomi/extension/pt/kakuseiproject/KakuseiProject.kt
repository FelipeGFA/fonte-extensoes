package eu.kanade.tachiyomi.extension.pt.kakuseiproject

import eu.kanade.tachiyomi.multisrc.madara.Madara
import keiyoushi.network.rateLimit
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)

class KakuseiProject :
    Madara(
        "Kakusei Project",
        "https://kakuseiproject.org",
        "pt-BR",
        dateFormat,
    ) {

    override val client: OkHttpClient = super.client.newBuilder()
        .addInterceptor(HtmlProxy(baseUrl))
        .addInterceptor(ImageProxy(baseUrl))
        .rateLimit(3, 1.seconds)
        .build()

    override val useNewChapterEndpoint = true

    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
