package eu.kanade.tachiyomi.extension.pt.sssscanlator

import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.lib.cryptoaes.CryptoAES
import keiyoushi.utils.parseAs
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal fun extractSeriesChapters(document: Document, mangaSlug: String): List<SeriesChapterDto> {
    require(mangaSlug.isNotBlank()) { "Slug da obra nao encontrado na URL" }

    val html = document.html()
    val base64Str = html.substringAfter("U2FsdGVkX1", missingDelimiterValue = "")
        .substringBefore('"')
        .let { if (it.isNotEmpty()) "U2FsdGVkX1$it" else null }

    return base64Str?.let {
        runCatching { it.decryptAndParseAs<List<SeriesChapterDto>>() }.getOrNull()
    }?.takeIf(List<SeriesChapterDto>::isNotEmpty)
        ?: error("Payload de capitulos nao encontrado para slug=$mangaSlug")
}

internal fun extractBadgeTexts(titleElement: Element?): List<String> {
    val nearbyElements = listOfNotNull(
        titleElement,
        titleElement?.previousElementSibling(),
        titleElement?.nextElementSibling(),
        titleElement?.nextElementSibling()?.nextElementSibling(),
        titleElement?.parent(),
        titleElement?.parent()?.parent(),
    )

    return nearbyElements
        .flatMap { element -> element.select("span[data-slot=badge]") }
        .map { it.text() }
        .filter(String::isNotEmpty)
        .distinct()
}

internal fun isStatusBadge(text: String): Boolean = parseStatus(text) != SManga.UNKNOWN

internal fun parseStatus(statusText: String?): Int = when (statusText?.lowercase()) {
    "em lancamento", "em lançamento", "ongoing" -> SManga.ONGOING
    "completo", "concluido", "concluído", "completed" -> SManga.COMPLETED
    "hiato", "hiatus" -> SManga.ON_HIATUS
    "cancelado", "canceled", "cancelled" -> SManga.CANCELLED
    else -> SManga.UNKNOWN
}

inline fun <reified T> String.decryptAndParseAs(): T = CryptoAES.decrypt(this, "yomu_trolling_scrapers_v1").parseAs<T>()
