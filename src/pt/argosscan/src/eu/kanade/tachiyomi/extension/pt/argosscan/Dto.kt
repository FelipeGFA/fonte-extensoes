package eu.kanade.tachiyomi.extension.pt.argosscan

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import java.text.SimpleDateFormat

@Serializable
class ProjectResponseDto(
    private val items: List<ProjectDto> = emptyList(),
) {
    fun toSMangaList(query: String = ""): List<SManga> = items
        .filterNot { it.isNovel() }
        .filter { it.matchesQuery(query) }
        .map { it.toSManga() }
}

@Serializable
class ProjectDto(
    private val id: String,
    private val title: String,
    private val slug: String,
    private val type: String? = null,
    private val description: String? = null,
    private val status: String? = null,
    @SerialName("cover_latest_url") private val coverLatestUrl: String? = null,
    private val authors: List<AuthorDto> = emptyList(),
    private val tags: List<TagDto> = emptyList(),
) {
    fun isNovel() = type.equals("Novel", ignoreCase = true)

    fun matchesQuery(query: String) = query.isEmpty() || title.contains(query, ignoreCase = true)

    fun toSManga() = SManga.create().apply {
        url = "/manga/$slug#$id"
        title = this@ProjectDto.title
        thumbnail_url = coverLatestUrl?.toThumbnailUrl()
        description = this@ProjectDto.description
        status = when (this@ProjectDto.status?.lowercase()) {
            "completo" -> SManga.COMPLETED
            "em lançamento" -> SManga.ONGOING
            "em pausa" -> SManga.ON_HIATUS
            else -> SManga.UNKNOWN
        }
        author = authors.filter { it.hasRole("autor") }
            .joinToString { it.name() }
            .takeIf { it.isNotBlank() }
        artist = authors.filter { it.hasRole("artista") }
            .joinToString { it.name() }
            .takeIf { it.isNotBlank() }
        genre = tags.joinToString { it.name() }.takeIf { it.isNotBlank() }
    }

    fun id() = id
}

@Serializable
class AuthorDto(
    private val name: String,
    private val role: String? = null,
) {
    fun hasRole(role: String) = this.role.equals(role, ignoreCase = true)

    fun name() = name
}

@Serializable
class TagDto(
    private val name: String,
) {
    fun name() = name
}

@Serializable
class ChapterResponseDto(
    private val items: List<ChapterDto> = emptyList(),
) {
    fun toSChapterList(projectId: String, dateFormat: SimpleDateFormat): List<SChapter> = items.sortedWith(compareByDescending<ChapterDto> { it.volumeNumber() }.thenByDescending { it.chapterNumber() })
        .map { it.toSChapter(projectId, dateFormat) }

    fun getImagesForChapter(chapterId: String, baseUrl: String): List<Page> = items.find { it.id() == chapterId }
        ?.toPages(baseUrl)
        ?: throw IOException("Capítulo não encontrado.")
}

@Serializable
class ChapterDto(
    private val id: String,
    private val title: String? = null,
    @SerialName("chapter_number") private val chapterNumber: Float? = null,
    @SerialName("volume_number") private val volumeNumber: Int? = null,
    @SerialName("created_at") private val createdAt: String? = null,
    private val images: List<ImageDto>? = null,
) {
    fun toSChapter(projectId: String, dateFormat: SimpleDateFormat) = SChapter.create().apply {
        url = "$id|$projectId"
        name = buildString {
            if (volumeNumber != null) append("Vol. $volumeNumber ")
            append("Cap. ")
            append(chapterNumber?.toString()?.removeSuffix(".0") ?: "0")
            if (!this@ChapterDto.title.isNullOrBlank()) append(" - ${this@ChapterDto.title}")
        }
        date_upload = dateFormat.tryParse(createdAt?.substringBefore("."))
    }

    fun id() = id

    fun volumeNumber() = volumeNumber

    fun chapterNumber() = chapterNumber

    fun toPages(baseUrl: String): List<Page> = images?.mapIndexed { index, image ->
        image.toPage(index, baseUrl)
    }.orEmpty()
}

@Serializable
class ImageDto(
    @SerialName("file_url") private val fileUrl: String,
) {
    fun toPage(index: Int, baseUrl: String): Page {
        val imageUrl = if (fileUrl.startsWith("http")) {
            fileUrl
        } else {
            "$baseUrl/${fileUrl.removePrefix("/")}"
        }

        return Page(index, imageUrl = imageUrl)
    }
}
