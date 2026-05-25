package eu.kanade.tachiyomi.extension.pt.sssscanlator

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Serializable
class LibraryResponseDto(
    @SerialName("obras")
    val mangas: List<LibraryMangaDto> = emptyList(),
    val series: List<LibraryMangaDto> = emptyList(),
    val pagination: LibraryPaginationDto = LibraryPaginationDto(),
)

@Serializable
class LibraryPaginationDto(
    val page: Int = 1,
    val totalPages: Int = 1,
)

@Serializable
class LibraryMangaDto(
    val title: String,
    val cover: String? = null,
    val slug: String,
) {
    fun toSManga(): SManga = SManga.create().apply {
        title = this@LibraryMangaDto.title
        thumbnail_url = cover?.takeUnless(String::isBlank)
        url = "/obra/$slug"
    }
}

@Serializable
class SeriesPayloadDto(
    val description: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val coverImage: String? = null,
    @SerialName("capitulos_lista")
    val chapters: List<SeriesChapterDto> = emptyList(),
    private val slug: String? = null,
)

@Serializable
class SeriesChapterDto(
    val number: Double,
    val title: String? = null,
    val releaseDate: String? = null,
    @SerialName("id")
    val chapterId: String,
    val releaseAt: String? = null,
) {
    fun toSChapter(mangaSlug: String): SChapter = SChapter.create().apply {
        val chapterNumberLabel = number.toChapterNumberString()

        url = "/ler/$mangaSlug/$chapterNumberLabel?chapterId=$chapterId"
        name = formatChapterTitle(title, chapterNumberLabel)
        chapter_number = number.toFloat()
        date_upload = parseChapterDate(releaseAt, releaseDate)
    }

    companion object {
        private val RELEASE_AT_MILLIS by lazy {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        }

        private val RELEASE_DATE by lazy {
            SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
        }

        private val CHAPTER_TITLE_PREFIX = Regex("^CAP[IÍ]TULO\\s+", RegexOption.IGNORE_CASE)

        private fun parseChapterDate(releaseAt: String?, releaseDate: String?): Long {
            RELEASE_AT_MILLIS.tryParse(releaseAt).takeIf { it != 0L }?.let { return it }
            return RELEASE_DATE.tryParse(releaseDate)
        }

        private fun formatChapterTitle(rawTitle: String?, chapterNumberLabel: String): String {
            val cleanedTitle = rawTitle?.takeUnless { it.isBlank() }
                ?: return "Capítulo $chapterNumberLabel"
            return cleanedTitle.replaceFirst(CHAPTER_TITLE_PREFIX, "Capítulo ")
        }
    }
}

@Serializable
class ChapterPageDto(
    val chapter: ChapterImagesDto,
)

@Serializable
class ChapterImagesDto(
    @SerialName("imagens_lista")
    val images: List<String>,
)

private fun Double.toChapterNumberString(): String = toString().removeSuffix(".0")
