package eu.kanade.tachiyomi.extension.pt.risentoons

import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat

private val microsecondsRegex = Regex("""(\.\d{3})\d+""")

@Serializable
internal class DataResponse<T>(
    private val data: T,
) {
    fun value(): T = data
}

@Serializable
internal class MangaListResponse(
    private val data: List<MangaDto>,
    private val limit: Int? = null,
    private val page: Int? = null,
    private val total: Int? = null,
) {
    fun toMangasPage(hasNextPage: Boolean = hasNextPage()): MangasPage = MangasPage(
        data.map { it.toSManga() },
        hasNextPage,
    )

    private fun hasNextPage(): Boolean {
        val page = page ?: return false
        val limit = limit ?: return false
        val total = total ?: return false
        return page * limit < total
    }
}

@Serializable
internal class MangaDto(
    private val id: String,
    private val title: String,
    private val slug: String,
    private val synopsis: String? = null,
    private val status: String? = null,
    @SerialName("cover_image") private val coverImage: String? = null,
    private val author: String? = null,
    private val artist: String? = null,
    private val genres: List<String> = emptyList(),
    @SerialName("alternative_names") private val alternativeNames: List<AlternativeNameDto> = emptyList(),
    @SerialName("manga_type") private val mangaType: String? = null,
    @SerialName("is_18") private val isAdult: Boolean = false,
    @SerialName("is_vip") private val isVip: Boolean = false,
) {
    fun id(): String = id

    fun toSManga(initialized: Boolean = false): SManga = SManga.create().apply {
        title = this@MangaDto.title
        url = "/biblioteca/$slug?id=$id"
        thumbnail_url = coverImage
        description = buildDescription()
        author = this@MangaDto.author
        artist = this@MangaDto.artist
        genre = genres.joinToString()
        status = this@MangaDto.status.toMangaStatus()
        this.initialized = initialized
    }

    private fun buildDescription(): String? = buildString {
        synopsis?.takeIf { it.isNotEmpty() }?.let { append(it) }

        alternativeNames
            .mapNotNull { it.nameOrNull() }
            .takeIf { it.isNotEmpty() }
            ?.let {
                if (isNotEmpty()) append("\n\n")
                append("Titulos alternativos: ${it.joinToString()}")
            }

        mangaType?.takeIf { it.isNotEmpty() }?.let {
            if (isNotEmpty()) append("\n\n")
            append("Tipo: $it")
        }

        if (isAdult || isVip) {
            if (isNotEmpty()) append("\n\n")
            append(
                listOfNotNull(
                    if (isAdult) "+18" else null,
                    if (isVip) "VIP" else null,
                ).joinToString(),
            )
        }
    }.takeIf { it.isNotEmpty() }
}

@Serializable
internal class AlternativeNameDto(
    private val name: String,
) {
    fun nameOrNull(): String? = name.takeIf { it.isNotEmpty() }
}

@Serializable
internal class ChapterListResponse(
    private val chapters: List<ChapterDto>,
) {
    fun toSChapterList(mangaSlug: String, dateFormat: SimpleDateFormat): List<SChapter> = chapters
        .map { it.toSChapter(mangaSlug, dateFormat) }
        .sortedWith(compareByDescending<SChapter> { it.chapter_number }.thenByDescending { it.date_upload })
}

@Serializable
internal class ChapterDto(
    private val id: String,
    private val title: String,
    private val number: Float,
    @SerialName("created_at") private val createdAt: String? = null,
) {
    fun toSChapter(mangaSlug: String, dateFormat: SimpleDateFormat): SChapter = SChapter.create().apply {
        val chapterNumber = number.toString().removeSuffix(".0")
        name = title.ifEmpty { "Capitulo $chapterNumber" }
        url = "/biblioteca/$mangaSlug/$chapterNumber/read?id=$id"
        chapter_number = number
        date_upload = dateFormat.tryParse(createdAt?.normalizeApiDate())
    }
}

@Serializable
internal class PageListResponse(
    private val pages: List<PageDto>,
) {
    fun toPageList(mediaUrl: String): List<Page> = pages
        .sortedBy { it.pageNumber() }
        .mapIndexed { index, page -> page.toPage(index, mediaUrl) }
}

@Serializable
internal class PageDto(
    @SerialName("image_url") private val imageUrl: String,
    @SerialName("page_number") private val pageNumber: Int,
) {
    fun pageNumber(): Int = pageNumber

    fun toPage(index: Int, mediaUrl: String): Page = Page(
        index,
        imageUrl = imageUrl.toAbsoluteUrl(mediaUrl),
    )
}

private fun String?.toMangaStatus(): Int = when (this?.lowercase()) {
    "ongoing" -> SManga.ONGOING
    "completed" -> SManga.COMPLETED
    "hiatus" -> SManga.ON_HIATUS
    "cancelled" -> SManga.CANCELLED
    else -> SManga.UNKNOWN
}

private fun String.normalizeApiDate(): String = replace(microsecondsRegex, "$1")

private fun String.toAbsoluteUrl(baseUrl: String): String = when {
    startsWith("http") -> this
    startsWith("//") -> "https:$this"
    startsWith("/") -> "$baseUrl$this"
    else -> "$baseUrl/$this"
}
