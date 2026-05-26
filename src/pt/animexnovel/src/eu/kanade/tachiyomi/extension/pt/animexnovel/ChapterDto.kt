package eu.kanade.tachiyomi.extension.pt.animexnovel

import eu.kanade.tachiyomi.source.model.SChapter
import kotlinx.serialization.Serializable

@Serializable
class ChapterDto(
    private val id: Long,
    private val link: String,
    private val title: String,
) {

    fun toSChapter(baseUrl: String): SChapter? {
        val chapterUrl = link.toHttps().takeIf(::isChapterUrl)
            ?: return null

        return SChapter.create().apply {
            name = title.toChapterName()
            url = chapterUrl.removePrefix(baseUrl)
            chapter_number = CHAPTER_NUMBER_REGEX.find(name)
                ?.groupValues
                ?.get(1)
                ?.replace(',', '.')
                ?.toFloatOrNull()
                ?: -1f
        }
    }

    private fun String.toChapterName(): String {
        val cleanTitle = replace("&#8211;", "–")
            .replace("&amp;", "&")

        return cleanTitle.substringAfter(" – ")
            .takeIf { it != cleanTitle && it.isNotBlank() }
            ?: cleanTitle.substringAfter(" - ")
                .takeIf { it != cleanTitle && it.isNotBlank() }
            ?: cleanTitle
    }

    private fun String.toHttps() = replace("http://", "https://")

    private fun isChapterUrl(url: String): Boolean = CHAPTER_SLUG_REGEX.containsMatchIn(url) || CHAPTER_NUMBER_REGEX.containsMatchIn(title)

    companion object {
        private val CHAPTER_NUMBER_REGEX = Regex("""(?i)cap[íi]tulo\s+(\d+(?:[.,]\d+)?)""")
        private val CHAPTER_SLUG_REGEX = Regex("""(?i)(?:^|[-/])capitulo(?:[-/]|$)""")
    }
}
