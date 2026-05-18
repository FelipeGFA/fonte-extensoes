package eu.kanade.tachiyomi.extension.pt.kuromangas

import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.text.SimpleDateFormat

@Serializable
class MangaListResponse(
    private val data: List<MangaDto>,
    private val pagination: PaginationDto,
) {
    fun toMangasPage(cdnUrl: String) = MangasPage(data.map { it.toSManga(cdnUrl) }, pagination.hasNextPage())
}

@Serializable
class PaginationDto(
    private val page: Int,
    @JsonNames("total_pages") private val totalPages: Int? = null,
    private val hasNext: Boolean? = null,
) {
    fun hasNextPage() = hasNext ?: (page < (totalPages ?: 1))
}

@Serializable
class MangaDto(
    private val id: Int,
    private val title: String,
    private val description: String? = null,
    private val status: String? = null,
    @SerialName("cover_image") private val coverImage: String? = null,
    private val author: String? = null,
    private val artist: String? = null,
    private val genres: List<String>? = null,
    @SerialName("alternative_titles") private val alternativeTitles: List<String>? = null,
) {
    fun toSManga(cdnUrl: String) = SManga.create().apply {
        url = "/manga/$id"
        title = this@MangaDto.title
        thumbnail_url = coverImage?.let { "$cdnUrl/${it.removePrefix("/").removePrefix("uploads/")}" }
        description = buildString {
            this@MangaDto.description?.let { append(it) }
            alternativeTitles?.takeIf { it.isNotEmpty() }?.let {
                if (isNotEmpty()) append("\n\n")
                append("Titulos alternativos: ${it.joinToString()}")
            }
        }.takeIf { it.isNotBlank() }
        author = this@MangaDto.author
        artist = this@MangaDto.artist
        genre = genres?.joinToString()
        status = when (this@MangaDto.status?.lowercase()) {
            "ongoing", "em andamento" -> SManga.ONGOING
            "completed", "completo" -> SManga.COMPLETED
            "hiatus", "em hiato" -> SManga.ON_HIATUS
            "cancelled", "cancelado" -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }
    }

    fun id() = id
}

@Serializable
class MangaDetailsResponse(
    private val manga: MangaDto,
    private val chapters: List<ChapterDto>,
) {
    fun toSManga(cdnUrl: String) = manga.toSManga(cdnUrl).apply {
        initialized = true
    }

    fun toChapterList(dateFormat: SimpleDateFormat) = chapters
        .map { it.toSChapter(manga.id(), dateFormat) }
        .sortedByDescending { it.chapter_number }
}

@Serializable
class ChapterDto(
    private val id: Int,
    private val title: String? = null,
    @SerialName("chapter_number") private val chapterNumber: String? = null,
    @SerialName("upload_date") private val uploadDate: String? = null,
) {
    fun toSChapter(mangaId: Int, dateFormat: SimpleDateFormat) = SChapter.create().apply {
        url = "/chapter/$mangaId/$id"
        name = buildString {
            chapterNumber?.toFloatOrNull()?.let { append("Capitulo ${it.toString().removeSuffix(".0")}") }
            this@ChapterDto.title?.takeIf { it.isNotBlank() }?.let {
                if (isNotEmpty()) append(" - ")
                append(it)
            }
        }.ifBlank { "Capitulo $id" }
        chapter_number = chapterNumber?.toFloatOrNull() ?: 0f
        date_upload = dateFormat.tryParse(uploadDate)
    }
}

@Serializable
class ChapterPagesResponse(private val pages: List<String>) {
    fun toPages(cdnUrl: String) = pages.mapIndexed { index, pageUrl ->
        val path = pageUrl.removePrefix("/uploads/").removePrefix("/")
        val imageUrl = if (pageUrl.startsWith("http")) pageUrl else "$cdnUrl/$path"
        Page(index, imageUrl = imageUrl)
    }
}

@Serializable
class ChapterReadRequest(private val page: Int)
