package eu.kanade.tachiyomi.extension.pt.valkyuri

import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private val microsecondDateRegex = Regex("""\.(\d{3})\d{3}Z$""")

private val repeatedChapterTitleRegex =
    Regex("""^cap[ií]tulo\s+0*([0-9]+(?:\.[0-9]+)?)(?:\s*(?:--|-|:|–|—)\s*(.*))?$""", RegexOption.IGNORE_CASE)

@Serializable
class HomeDiscoveryResponseDto(
    private val data: HomeDiscoveryDataDto? = null,
) {
    fun toMangasPage(sectionKey: String): MangasPage {
        val mangas = data?.section(sectionKey)
            ?.items()
            ?.map { it.toSManga() }
            .orEmpty()

        return MangasPage(mangas, hasNextPage = false)
    }
}

@Serializable
class HomeDiscoveryDataDto(
    private val sections: List<HomeSectionDto> = emptyList(),
) {
    fun section(key: String): HomeSectionDto? = sections.firstOrNull { it.matches(key) }
}

@Serializable
class HomeSectionDto(
    private val key: String,
    private val items: List<MangaDto> = emptyList(),
) {
    fun matches(otherKey: String): Boolean = key == otherKey

    fun items(): List<MangaDto> = items
}

@Serializable
class MangaListResponseDto(
    private val data: List<MangaDto> = emptyList(),
    private val meta: PaginationMetaDto? = null,
) {
    fun toMangasPage(): MangasPage {
        val mangas = data.map { it.toSManga() }
        return MangasPage(mangas, hasNextPage = meta?.hasNextPage() ?: false)
    }
}

@Serializable
class LatestReleasesResponseDto(
    private val data: List<ReleaseDto> = emptyList(),
    private val meta: PaginationMetaDto? = null,
) {
    fun toMangasPage(): MangasPage {
        val mangas = data.mapNotNull { it.toSManga() }
            .distinctBy { it.url }

        return MangasPage(mangas, hasNextPage = meta?.hasNextPage() ?: false)
    }
}

@Serializable
class MangaDetailsResponseDto(
    private val data: MangaDto,
) {
    fun toSManga(): SManga = data.toSManga(isDetails = true)

    fun toChapterList(): List<SChapter> = data.toChapterList()
}

@Serializable
class ChapterDetailsResponseDto(
    private val data: ReaderDataDto? = null,
) {
    fun toPages(): List<Page> = data?.toPages().orEmpty()
}

@Serializable
class ReleaseDto(
    private val manga: MangaDto? = null,
) {
    fun toSManga(): SManga? = manga?.toSManga()
}

@Serializable
class MangaDto(
    private val name: String,
    private val slug: String,
    private val description: String? = null,
    @SerialName("cover_url")
    private val coverUrl: String? = null,
    @SerialName("type_label")
    private val typeLabel: String? = null,
    private val status: String? = null,
    @SerialName("status_label")
    private val statusLabel: String? = null,
    @SerialName("views_count")
    private val viewsCount: Int? = null,
    @SerialName("is_adult")
    private val isAdult: Boolean? = null,
    @SerialName("followers_count")
    private val followersCount: Int? = null,
    @SerialName("chapters_count")
    private val chaptersCount: Int? = null,
    private val genres: List<GenreDto>? = null,
    private val chapters: List<ChapterDto>? = null,
) {
    fun toSManga(isDetails: Boolean = false): SManga = SManga.create().apply {
        url = slug
        title = name
        thumbnail_url = coverUrl
        genre = genres.orEmpty().joinToString { it.name() }
        status = this@MangaDto.status.toStatus()

        if (isDetails) {
            initialized = true
            description = buildDescription()
        } else {
            description = this@MangaDto.description
        }
    }

    fun toChapterList(): List<SChapter> = chapters.orEmpty()
        .map { it.toSChapter(slug) }
        .sortedWith(
            compareByDescending<SChapter> { it.chapter_number }
                .thenByDescending { it.date_upload },
        )

    private fun buildDescription(): String = buildString {
        if (!description.isNullOrEmpty()) {
            append(description)
        }

        val info = listOfNotNull(
            typeLabel?.let { "Tipo: $it" },
            statusLabel?.let { "Status: $it" },
            chaptersCount?.let { "Capítulos: $it" },
            viewsCount?.let { "Visualizações: $it" },
            followersCount?.let { "Seguidores: $it" },
            isAdult?.let { "Conteúdo adulto: ${if (it) "Sim" else "Não"}" },
        )

        if (info.isNotEmpty()) {
            if (isNotEmpty()) {
                append("\n\n")
            }
            append(info.joinToString("\n"))
        }
    }
}

@Serializable
class GenreDto(
    private val name: String,
) {
    fun name(): String = name
}

@Serializable
class ChapterDto(
    private val number: String,
    @SerialName("number_sort")
    private val numberSort: Float? = null,
    private val title: String? = null,
    @SerialName("published_at")
    private val publishedAt: String? = null,
    @SerialName("created_at")
    private val createdAt: String? = null,
) {
    fun toSChapter(mangaSlug: String): SChapter = SChapter.create().apply {
        url = "/reader/$mangaSlug/$number"
        name = buildName()
        chapter_number = numberSort ?: number.toFloatOrNull() ?: -1f
        date_upload = (publishedAt ?: createdAt).toUploadDate()
    }

    private fun buildName(): String {
        val chapterName = "Capítulo $number"
        return title?.trim()
            ?.removeDuplicateChapterPrefix()
            ?.takeIf { it.isNotEmpty() }
            ?.let { "$chapterName - $it" }
            ?: chapterName
    }

    private fun String.removeDuplicateChapterPrefix(): String {
        val match = repeatedChapterTitleRegex.matchEntire(this) ?: return this
        val titleNumber = match.groupValues
            .getOrNull(1)
            ?.toFloatOrNull()

        return if (titleNumber == number.toFloatOrNull()) {
            match.groupValues.getOrNull(2).orEmpty()
        } else {
            this
        }
    }
}

@Serializable
class ReaderDataDto(
    private val chapter: ReaderChapterDto? = null,
) {
    fun toPages(): List<Page> = chapter?.toPages().orEmpty()
}

@Serializable
class ReaderChapterDto(
    private val pages: List<PageDto>? = null,
) {
    fun toPages(): List<Page> = pages.orEmpty()
        .filter { it.imageUrl() != null }
        .sortedBy { it.pageNumber() }
        .mapIndexed { index, page ->
            Page(index, imageUrl = page.imageUrl())
        }
}

@Serializable
class PageDto(
    @SerialName("page_number")
    private val pageNumber: Int,
    @SerialName("image_url")
    private val imageUrl: String? = null,
) {
    fun pageNumber(): Int = pageNumber

    fun imageUrl(): String? = imageUrl
}

@Serializable
class PaginationMetaDto(
    @SerialName("current_page")
    private val currentPage: Int = 1,
    @SerialName("last_page")
    private val lastPage: Int = 1,
) {
    fun hasNextPage(): Boolean = currentPage < lastPage
}

private fun String?.toStatus(): Int {
    val normalizedStatus = this?.lowercase(Locale.ROOT) ?: return SManga.UNKNOWN
    return when (normalizedStatus) {
        "ongoing", "em andamento" -> SManga.ONGOING
        "completed", "concluído", "concluido" -> SManga.COMPLETED
        "hiatus", "hiato" -> SManga.ON_HIATUS
        "cancelled", "cancelado" -> SManga.CANCELLED
        else -> SManga.UNKNOWN
    }
}

private fun String?.toUploadDate(): Long = apiDateFormat.tryParse(this?.replace(microsecondDateRegex, ".${'$'}1Z"))
