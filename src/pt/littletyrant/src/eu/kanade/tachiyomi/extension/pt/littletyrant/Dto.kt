package eu.kanade.tachiyomi.extension.pt.littletyrant

import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
class ChapterDto(
    private val `data`: HtmlDto,
) {
    fun isEmpty() = data.html().isBlank()
    fun toJsoup(baseUrl: String): Document = Jsoup.parseBodyFragment(data.html(), baseUrl)
}

@Serializable
class HtmlDto(
    private val html: String,
) {
    fun html(): String = html
}

@Serializable
class TokenDto(
    private val token: String,
) {
    fun token(): String = token
}
