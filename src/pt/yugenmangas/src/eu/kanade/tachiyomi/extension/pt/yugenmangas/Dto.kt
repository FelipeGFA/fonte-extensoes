package eu.kanade.tachiyomi.extension.pt.yugenmangas

import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat

@Serializable
class LibraryPageDto(
    private val initialData: LibraryDto,
) {
    fun toMangasPage(imageBaseUrl: String): MangasPage = initialData.toMangasPage(imageBaseUrl)
}

@Serializable
class LibraryDto(
    private val series: List<LibraryMangaDto>? = null,
    private val pagination: PaginationDto? = null,
) {
    fun toMangasPage(imageBaseUrl: String): MangasPage {
        val mangas = series.orEmpty().mapNotNull { it.toSManga(imageBaseUrl) }
        return MangasPage(mangas, pagination?.hasNextPage() ?: false)
    }
}

@Serializable
class PaginationDto(
    @SerialName("current_page")
    private val currentPage: Int = 1,
    @SerialName("total_pages")
    private val totalPages: Int = 1,
    @SerialName("has_next")
    private val hasNext: Boolean? = null,
) {
    fun hasNextPage(): Boolean = hasNext ?: (currentPage < totalPages)
}

@Serializable
class LibraryMangaDto(
    private val code: String,
    private val title: String,
    private val cover: String? = null,
) {
    fun toSManga(imageBaseUrl: String): SManga? {
        if (code.isEmpty() || title.isEmpty()) return null

        return SManga.create().apply {
            title = this@LibraryMangaDto.title
            url = "/series/$code"
            thumbnail_url = cover
                ?.takeIf(String::isNotEmpty)
                ?.let { "$imageBaseUrl/$it" }
        }
    }
}

@Serializable
class LatestPageDto(
    private val initialData: LatestDto,
) {
    fun toMangasPage(imageBaseUrl: String): MangasPage = initialData.toMangasPage(imageBaseUrl)
}

@Serializable
class LatestDto(
    private val updates: List<LatestUpdateDto> = emptyList(),
    private val pagination: PaginationDto,
) {
    fun toMangasPage(imageBaseUrl: String): MangasPage {
        val mangas = updates
            .mapNotNull { it.toSManga(imageBaseUrl) }
            .distinctBy { it.url }

        return MangasPage(mangas, pagination.hasNextPage())
    }
}

@Serializable
class LatestUpdateDto(
    private val series: LatestMangaDto,
) {
    fun toSManga(imageBaseUrl: String): SManga? = series.toSManga(imageBaseUrl)
}

@Serializable
class LatestMangaDto(
    private val code: String,
    private val name: String,
    private val cover: String? = null,
) {
    fun toSManga(imageBaseUrl: String): SManga? {
        if (code.isEmpty() || name.isEmpty()) return null

        return SManga.create().apply {
            title = this@LatestMangaDto.name
            url = "/series/$code"
            thumbnail_url = cover
                ?.takeIf(String::isNotEmpty)
                ?.let { "$imageBaseUrl/$it" }
        }
    }
}

@Serializable
class DetailsPageDto(
    private val seriesData: DetailsMangaDto,
) {
    fun toSManga(imageBaseUrl: String, url: String): SManga = seriesData.toSManga(imageBaseUrl, url)

    fun toSChapterList(dateFormat: SimpleDateFormat): List<SChapter> = seriesData.toSChapterList(dateFormat)

    val currentChapterPage: Int
        get() = seriesData.currentChapterPage

    val totalChapterPages: Int
        get() = seriesData.totalChapterPages
}

@Serializable
class DetailsMangaDto(
    private val title: String,
    private val coverImage: String? = null,
    private val status: String? = null,
    private val statusOriginal: String? = null,
    private val genres: List<String>? = null,
    private val author: DetailsNameDto? = null,
    private val artist: DetailsNameDto? = null,
    private val description: List<String>? = null,
    private val chapters: DetailsChaptersDto? = null,
) {
    fun toSManga(imageBaseUrl: String, url: String): SManga = SManga.create().apply {
        this.url = url
        this.title = this@DetailsMangaDto.title
        thumbnail_url = coverImage
            ?.takeIf(String::isNotEmpty)
            ?.let { "$imageBaseUrl/$it" }
        author = this@DetailsMangaDto.author?.displayName()
        artist = this@DetailsMangaDto.artist?.displayName()
        genre = genres
            .orEmpty()
            .joinToString(", ")
            .takeIf(String::isNotEmpty)
        status = (statusOriginal ?: this@DetailsMangaDto.status).toMangaStatus()
        description = this@DetailsMangaDto.description
            .orEmpty()
            .filter(String::isNotEmpty)
            .joinToString("\n\n")
            .takeIf(String::isNotEmpty)
        initialized = true
    }

    fun toSChapterList(dateFormat: SimpleDateFormat): List<SChapter> = chapters?.toSChapterList(dateFormat).orEmpty()

    val currentChapterPage: Int
        get() = chapters?.currentPage() ?: 1

    val totalChapterPages: Int
        get() = chapters?.totalPages() ?: 1
}

@Serializable
class DetailsNameDto(
    private val name: String? = null,
) {
    fun displayName(): String? = name?.takeIf(String::isNotEmpty)
}

@Serializable
class DetailsChaptersDto(
    private val chapters: List<DetailsChapterDto>? = null,
    @SerialName("current_page")
    private val currentPage: Int = 1,
    @SerialName("total_pages")
    private val totalPages: Int = 1,
) {
    fun toSChapterList(dateFormat: SimpleDateFormat): List<SChapter> = chapters.orEmpty().mapNotNull { it.toSChapter(dateFormat) }

    fun currentPage(): Int = currentPage

    fun totalPages(): Int = totalPages
}

@Serializable
class DetailsChapterDto(
    private val number: String,
    private val code: String,
    private val title: String? = null,
    @SerialName("created_at")
    private val createdAt: String? = null,
) {
    fun toSChapter(dateFormat: SimpleDateFormat): SChapter? {
        if (number.isEmpty() || code.isEmpty()) return null

        return SChapter.create().apply {
            url = "/reader/$code"
            name = buildString {
                append("Capitulo ")
                append(number.removeSuffix(".0"))

                title?.takeIf(String::isNotEmpty)?.let {
                    append(" - ")
                    append(it)
                }
            }
            date_upload = dateFormat.tryParse(createdAt)
        }
    }
}

@Serializable
class ReaderPageDto(
    private val chapterData: ReaderChapterDataDto,
) {
    fun toPageList(imageBaseUrl: String): List<Page> = chapterData.toPageList(imageBaseUrl)
}

@Serializable
class ReaderChapterDataDto(
    private val chapter: ReaderChapterDto,
) {
    fun toPageList(imageBaseUrl: String): List<Page> = chapter.toPageList(imageBaseUrl)
}

@Serializable
class ReaderChapterDto(
    private val pages: List<ReaderImageDto>? = null,
) {
    fun toPageList(imageBaseUrl: String): List<Page> = pages
        .orEmpty()
        .filter { it.hasPath() }
        .sortedBy { it.pageNumber() }
        .mapIndexed { index, page -> Page(index, imageUrl = page.imageUrl(imageBaseUrl)) }
}

@Serializable
class ReaderImageDto(
    private val path: String? = null,
    private val number: Int? = null,
) {
    fun hasPath(): Boolean = path?.isNotEmpty() == true

    fun pageNumber(): Int = number ?: Int.MAX_VALUE

    fun imageUrl(imageBaseUrl: String): String {
        val path = path.orEmpty()
        val imageUrl = when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("/") -> "$imageBaseUrl$path"
            else -> "$imageBaseUrl/$path"
        }

        return imageUrl
    }
}

private fun String?.toMangaStatus(): Int = when (this?.lowercase()) {
    "ongoing", "em andamento", "em lancamento", "em lançamento" -> SManga.ONGOING
    "completed", "finalizada", "completo", "concluido", "concluído" -> SManga.COMPLETED
    "hiatus", "hiato" -> SManga.ON_HIATUS
    "canceled", "cancelled", "cancelada", "dropped", "abandonada" -> SManga.CANCELLED
    else -> SManga.UNKNOWN
}
