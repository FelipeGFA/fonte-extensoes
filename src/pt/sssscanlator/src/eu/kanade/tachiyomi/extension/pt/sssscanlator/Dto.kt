package eu.kanade.tachiyomi.extension.pt.sssscanlator

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Serializable
class LibraryResponseDto(
    val pagination: LibraryPaginationDto = LibraryPaginationDto(),
    val garimpo: String? = null,
)

@Serializable
class LibraryPaginationDto(
    val page: Int = 1,
    val totalPages: Int = 1,
)

@Serializable
class LibraryMangaDto(
    private val title: String,
    private val cover: String? = null,
    private val slug: String,
    private val type: String? = null,
) {
    val isNovel: Boolean get() = type == "novel"

    fun toSManga(): SManga = SManga.create().apply {
        title = this@LibraryMangaDto.title
        thumbnail_url = cover?.takeUnless(String::isBlank)
        url = "/obra/$slug"
    }
}

@Serializable
class SeriesChapterDto(
    private val number: Double,
    private val title: String? = null,
    private val releaseDate: String? = null,
    @SerialName("id")
    private val chapterId: String,
    private val releaseAt: String? = null,
) {
    fun toSChapter(mangaSlug: String): SChapter = SChapter.create().apply {
        val chapterNumberLabel = number.toChapterNumberString()

        url = "/ler/$mangaSlug/$chapterNumberLabel?chapterId=$chapterId"
        name = title?.takeUnless { it.isBlank() } ?: "Capítulo $chapterNumberLabel"
        chapter_number = number.toFloat()
        date_upload = parseChapterDate(releaseAt, releaseDate)
    }

    companion object {
        private val RELEASE_AT_MILLIS = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        private val RELEASE_DATE = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)

        private fun parseChapterDate(releaseAt: String?, releaseDate: String?): Long {
            RELEASE_AT_MILLIS.tryParse(releaseAt).takeIf { it != 0L }?.let { return it }
            return RELEASE_DATE.tryParse(releaseDate)
        }
    }
}

@Serializable
class ChapterImagesDto(
    @SerialName("imagens_lista")
    val images: List<String>,
)

private fun Double.toChapterNumberString(): String = toString().removeSuffix(".0")
