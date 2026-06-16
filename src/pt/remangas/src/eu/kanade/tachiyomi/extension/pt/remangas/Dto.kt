package eu.kanade.tachiyomi.extension.pt.remangas

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
class PageableDto<T>(
    @JsonNames("chapters", "comics")
    val list: List<T>,
    private val page: Long,
    @SerialName("total_pages")
    private val totalPages: Long = 0,
) {
    fun hasNextPage() = page < totalPages
}

@Serializable
class MangaDto(
    val slug: String,
    val title: String,
    val cover: String? = null,
) {
    fun toSManga() = SManga.create().apply {
        title = this@MangaDto.title
        thumbnail_url = cover
        url = "/manga/$slug"
    }
}

@Serializable
class ChapterListJsonLdDto(
    private val itemListElement: List<ChapterListItemDto>,
) {
    fun toChapterList(setChapterUrl: SChapter.(String) -> Unit): List<SChapter> = itemListElement.map { it.toSChapter(setChapterUrl) }
}

@Serializable
class ChapterListItemDto(
    private val item: ChapterJsonLdDto,
) {
    fun toSChapter(setChapterUrl: SChapter.(String) -> Unit): SChapter = item.toSChapter(setChapterUrl)
}

@Serializable
class ChapterJsonLdDto(
    private val name: String,
    private val url: String,
    private val position: Float,
) {
    fun toSChapter(setChapterUrl: SChapter.(String) -> Unit): SChapter = SChapter.create().apply {
        name = this@ChapterJsonLdDto.name
        chapter_number = position
        setChapterUrl(this@ChapterJsonLdDto.url)
    }
}
