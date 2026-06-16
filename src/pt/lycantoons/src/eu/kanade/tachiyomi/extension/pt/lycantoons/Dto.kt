package eu.kanade.tachiyomi.extension.pt.lycantoons

import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Serializable
class PopularResponse(
    private val data: List<SeriesDto>,
    private val pagination: PaginationDto? = null,
) {
    fun toMangasPage(): MangasPage = MangasPage(
        data.map(SeriesDto::toSManga),
        pagination?.hasNextPage() ?: false,
    )
}

@Serializable
class SearchResponse(
    private val series: List<SeriesDto>,
) {
    fun toMangasPage(): MangasPage = MangasPage(series.map(SeriesDto::toSManga), false)
}

@Serializable
class PaginationDto(
    private val page: Int? = null,
    private val totalPages: Int? = null,
    private val hasNext: Boolean? = null,
) {
    fun hasNextPage(): Boolean = hasNext ?: (page != null && totalPages != null && page < totalPages)
}

@Serializable
class SeriesDto(
    private val title: String,
    private val slug: String,
    private val coverUrl: String? = null,
    private val author: String? = null,
    private val artist: String? = null,
    private val description: String? = null,
    private val genre: List<String>? = null,
    private val status: String? = null,
    private val capitulos: List<ChapterDto>? = null,
) {
    fun toSManga(): SManga = SManga.create().apply {
        title = this@SeriesDto.title
        url = "/series/$slug"
        thumbnail_url = coverUrl
        author = this@SeriesDto.author?.takeIf(String::isNotBlank)
        artist = this@SeriesDto.artist?.takeIf(String::isNotBlank)
        genre = this@SeriesDto.genre?.takeIf(List<String>::isNotEmpty)?.joinToString()
        description = this@SeriesDto.description
        status = parseStatus(this@SeriesDto.status)
    }

    fun toChapterList(): List<SChapter> = capitulos.orEmpty()
        .map { it.toSChapter(slug) }
        .sortedByDescending(SChapter::chapter_number)
}

@Serializable
class SearchRequestBody(
    val limit: Int,
    val page: Int,
    val search: String,
    val seriesType: String,
    val status: String,
    val tags: List<String>,
)

@Serializable
class ChapterDto(
    private val numero: JsonElement,
    private val createdAt: String? = null,
    private val pageCount: Int? = null,
) {
    fun toSChapter(slug: String): SChapter = SChapter.create().apply {
        val numberString = numero.jsonPrimitive.content
        name = "Capítulo $numberString"
        val pagesQuery = pageCount?.let { "?pages=$it" }.orEmpty()
        url = "/series/$slug/$numberString$pagesQuery"
        date_upload = dateFormat.tryParse(createdAt)
        chapter_number = numberString.toFloatOrNull() ?: -1f
    }
}

@Serializable
class ChapterPageDto(
    private val imageUrls: List<String>,
) {
    fun toPageList(): List<Page> = imageUrls.mapIndexed { index, imageUrl ->
        Page(index, imageUrl = imageUrl)
    }
}

private fun parseStatus(status: String?): Int = when (status?.lowercase(Locale.ROOT)) {
    "ongoing" -> SManga.ONGOING
    "completed" -> SManga.COMPLETED
    "hiatus" -> SManga.ON_HIATUS
    "cancelled" -> SManga.CANCELLED
    else -> SManga.UNKNOWN
}

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
