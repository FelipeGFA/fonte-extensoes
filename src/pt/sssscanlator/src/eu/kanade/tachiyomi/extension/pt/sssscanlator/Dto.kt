package eu.kanade.tachiyomi.extension.pt.sssscanlator

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal const val LOCKED_CHAPTER_ERROR = "Capítulo ainda não liberado"
internal const val LOCK_SYMBOL = "\uD83D\uDD12"
internal const val RELEASE_AT_QUERY = "releaseAt"

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

private val RELEASE_AT_MILLIS by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

@Serializable
class LibraryResponseDto(
    @SerialName("acervo")
    private val library: List<LibraryMangaDto> = emptyList(),
    @SerialName("catalogo")
    private val catalog: List<LibraryMangaDto> = emptyList(),
    @SerialName("obras")
    private val mangas: List<LibraryMangaDto> = emptyList(),
    private val series: List<LibraryMangaDto> = emptyList(),
    val pagination: LibraryPaginationDto = LibraryPaginationDto(),
) {
    fun toMangaList(): List<SManga> {
        val entries = library.ifEmpty { catalog.ifEmpty { mangas + series } }
        return entries
            .map { it.toSManga() }
            .distinctBy { it.url }
    }
}

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
        val releaseAtMillis = parseReleaseAt(releaseAt)
        val isLocked = releaseAtMillis > System.currentTimeMillis()

        url = buildString {
            append("/ler/")
            append(mangaSlug)
            append("/")
            append(chapterNumberLabel)
            append("?chapterId=")
            append(chapterId)
            if (isLocked) {
                append("&")
                append(RELEASE_AT_QUERY)
                append("=")
                append(releaseAtMillis)
            }
        }
        name = buildString {
            append(title?.takeUnless { it.isBlank() } ?: "Capítulo $chapterNumberLabel")
            if (isLocked) {
                append(" ")
                append(LOCK_SYMBOL)
            }
        }
        chapter_number = number.toFloat()
        date_upload = releaseAtMillis.takeIf { it != 0L } ?: parseChapterDate(releaseDate)
    }

    companion object {
        private val RELEASE_DATE by lazy {
            SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
        }

        private fun parseChapterDate(releaseDate: String?): Long = RELEASE_DATE.tryParse(releaseDate)
    }
}

@Serializable
class ChapterPageDto(
    val chapter: ChapterImagesDto,
    val lockedInfo: LockedInfoDto? = null,
) {
    fun toPageList(): List<Page> = chapter.images.mapIndexed { index, imageUrl ->
        Page(index, imageUrl = imageUrl)
    }
}

@Serializable
class ChapterImagesDto(
    @SerialName("imagens_lista")
    val images: List<String>,
)

@Serializable
class LockedInfoDto(
    val type: String? = null,
    val releaseAt: String? = null,
) {
    fun errorMessage(): String = lockedChapterError(parseReleaseAt(releaseAt))
}

internal fun parseReleaseAt(releaseAt: String?): Long = RELEASE_AT_MILLIS.tryParse(releaseAt?.removePrefix("\$D"))

internal fun lockedChapterError(releaseAt: Long): String {
    val remaining = releaseAt - System.currentTimeMillis()
    if (remaining <= 0L) return LOCKED_CHAPTER_ERROR

    return "$LOCKED_CHAPTER_ERROR. Será liberado em ${remaining.toReadableDuration()}"
}

private fun Long.toReadableDuration(): String {
    if (this < MINUTE_MILLIS) return "menos de 1 minuto"

    val days = this / DAY_MILLIS
    val hours = (this % DAY_MILLIS) / HOUR_MILLIS
    val minutes = (this % HOUR_MILLIS) / MINUTE_MILLIS

    val parts = mutableListOf<String>()
    if (days > 0) parts += days.toPlural("dia", "dias")
    if (hours > 0) parts += hours.toPlural("hora", "horas")
    if (days == 0L && minutes > 0) parts += minutes.toPlural("minuto", "minutos")

    return parts.take(2).joinToString(" ")
}

private fun Long.toPlural(singular: String, plural: String): String = "$this ${if (this == 1L) singular else plural}"

private fun Double.toChapterNumberString(): String = toString().removeSuffix(".0")
