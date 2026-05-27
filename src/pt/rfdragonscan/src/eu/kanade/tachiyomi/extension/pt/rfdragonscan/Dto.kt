package eu.kanade.tachiyomi.extension.pt.rfdragonscan

import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private const val LOCK_SYMBOL = "\uD83D\uDD12"
internal const val CHAPTER_ACCESS_TYPE_QUERY = "accessType"
internal const val COINS_ACCESS_TYPE = "COINS"
internal const val COINS_CHAPTER_ERROR = "Libere o capitulo na webview"
private const val FREE_ACCESS_TYPE = "FREE"

private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

@Serializable
class LatestDto(
    @Suppress("unused")
    private val lastProjects: List<JsonElement>,
    private val lastUpdates: List<MangaDto>,
) {
    fun toMangasPage(): MangasPage {
        val mangas = lastUpdates
            .mapNotNull(MangaDto::toSManga)
            .distinctBy { it.url }
        return MangasPage(mangas, false)
    }
}

@Serializable
class ProjectsPageDto(
    private val projects: List<MangaDto>,
    private val pagination: PaginationDto,
) {
    fun toMangasPage(): MangasPage {
        val mangas = projects
            .mapNotNull(MangaDto::toSManga)
            .distinctBy { it.url }
        return MangasPage(mangas, pagination.hasNextPage())
    }
}

@Serializable
class PaginationDto(
    private val hasNextPage: Boolean = false,
) {
    fun hasNextPage(): Boolean = hasNextPage
}

@Serializable
class MangaDto(
    private val id: String,
    private val title: String,
    @SerialName("cover_image")
    private val coverImage: String? = null,
    @SerialName("link")
    private val slug: String,
    private val status: String = "",
) {
    fun toSManga(): SManga? {
        if (id.isEmpty() || title.isEmpty() || slug.isEmpty()) return null

        return SManga.create().apply {
            title = this@MangaDto.title
            thumbnail_url = coverImage
            url = "/${this@MangaDto.id}/${this@MangaDto.slug}"
            status = this@MangaDto.status.toMangaStatus()
        }
    }
}

@Serializable
class MangaDetailsDto(
    private val title: String,
    @SerialName("alt_titles")
    private val altTitles: List<String> = emptyList(),
    @SerialName("cover_image")
    private val coverImage: String? = null,
    private val status: String = "",
    @SerialName("synopsis")
    private val description: String? = null,
    private val type: String? = null,
    private val flag: String? = null,
    @SerialName("year_released")
    private val yearReleased: Int? = null,
    private val genders: List<ValueDto> = emptyList(),
    private val artists: List<ValueDto> = emptyList(),
    private val authors: List<ValueDto> = emptyList(),
) {
    fun toSManga(): SManga = SManga.create().apply {
        title = this@MangaDetailsDto.title
        thumbnail_url = coverImage
        status = this@MangaDetailsDto.status.toMangaStatus()
        genre = genders.joinToString { it.name() }.takeIf(String::isNotEmpty)
        author = authors.joinToString { it.name() }.takeIf(String::isNotEmpty)
        artist = artists.joinToString { it.name() }.takeIf(String::isNotEmpty)
        description = buildString {
            this@MangaDetailsDto.description?.takeIf(String::isNotEmpty)?.let { append(it) }
            appendInfo("Titulos alternativos", altTitles.joinToString().takeIf(String::isNotEmpty))
            appendInfo("Tipo", type)
            appendInfo("Pais", flag)
            appendInfo("Ano", yearReleased?.toString())
        }.takeIf(String::isNotEmpty)
        initialized = true
    }

    @Serializable
    class ValueDto(
        private val name: String,
    ) {
        fun name(): String = name
    }
}

@Serializable
class ChapterGroupsDto(
    private val groupName: String = "Temporada",
    private val groups: List<ChapterGroupDto>,
) {
    fun toSChapterList(mangaPath: String): List<SChapter> = groups
        .flatMap { group -> group.toSChapterList(mangaPath, groupName) }
        .sortedByDescending { it.chapter_number }
}

@Serializable
class ChapterGroupDto(
    private val name: Double? = null,
    private val chapters: List<ChapterDto> = emptyList(),
) {
    fun toSChapterList(mangaPath: String, groupName: String): List<SChapter> = chapters.map { it.toSChapter(mangaPath, groupName, name) }
}

@Serializable
class ChapterDto(
    private val title: Double,
    @SerialName("created_at")
    private val createdAt: String? = null,
    private val text: String? = null,
    private val accessType: String = "FREE",
) {
    fun toSChapter(mangaPath: String, groupName: String, groupNumber: Double?): SChapter {
        val numberText = title.toString().removeSuffix(".0")
        val groupText = groupNumber?.toString()?.removeSuffix(".0")
        val isLocked = accessType != FREE_ACCESS_TYPE

        return SChapter.create().apply {
            url = buildString {
                append(mangaPath)
                append("/capitulo/")
                append(numberText.replace(".", "-"))
                if (isLocked) {
                    append("?")
                    append(CHAPTER_ACCESS_TYPE_QUERY)
                    append("=")
                    append(accessType)
                }
            }
            name = buildString {
                if (!groupText.isNullOrEmpty()) {
                    append(groupName)
                    append(" ")
                    append(groupText)
                    append(" - ")
                }
                append("Capitulo ")
                append(numberText)
                text?.takeIf(String::isNotEmpty)?.let {
                    append(" - ")
                    append(it)
                }
                if (isLocked) {
                    append(" ")
                    append(LOCK_SYMBOL)
                }
            }
            chapter_number = title.toFloat()
            date_upload = DATE_FORMAT.tryParse(createdAt)
        }
    }
}

@Serializable
class PagesDto(
    private val pages: List<PageDto>,
) {
    fun toPageList(referer: String): List<Page> = pages
        .sortedBy { it.pageNumber() }
        .mapIndexed { index, page ->
            Page(index, url = referer, imageUrl = page.photo())
        }
}

@Serializable
class PageDto(
    private val pageNumber: Int,
    private val photo: String,
) {
    fun pageNumber(): Int = pageNumber
    fun photo(): String = photo
}

private fun StringBuilder.appendInfo(label: String, value: String?) {
    value?.takeIf(String::isNotEmpty)?.let {
        if (isNotEmpty()) append("\n\n")
        append(label)
        append(": ")
        append(it)
    }
}

internal fun JsonElement.isLatestPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return "lastProjects" in payload && "lastUpdates" in payload
}

internal fun JsonElement.isProjectsPagePayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return "projects" in payload && "pagination" in payload
}

internal fun JsonElement.isMangaDetailsPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return "title" in payload && "synopsis" in payload && "genders" in payload
}

internal fun JsonElement.isChapterGroupsPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return "groupName" in payload && "groups" in payload
}

internal fun JsonElement.isPagesPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return "pages" in payload
}

private fun String.toMangaStatus(): Int = when (lowercase()) {
    "active", "up_to_date", "coming_soon" -> SManga.ONGOING
    "hiatus" -> SManga.ON_HIATUS
    "finished" -> SManga.COMPLETED
    else -> SManga.UNKNOWN
}
