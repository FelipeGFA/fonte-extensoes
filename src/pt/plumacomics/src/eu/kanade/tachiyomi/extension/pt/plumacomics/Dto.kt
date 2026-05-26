package eu.kanade.tachiyomi.extension.pt.plumacomics

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.text.SimpleDateFormat

internal const val VIP_CHAPTER_ERROR = "Capitulo apenas para VIP"
internal const val VIP_CHAPTER_QUERY = "vip=1"
internal const val VIP_UNLOCK_AT_QUERY = "unlockAt"

private const val LOCK_SYMBOL = "🔒"

@Serializable
class SearchDto(
    private val results: List<MangaDto> = emptyList(),
) {
    fun toMangaList(baseUrl: String): List<SManga> = results.mapNotNull { it.toSManga(baseUrl) }
}

@Serializable
class LatestPayloadDto(
    private val items: List<LatestItemDto> = emptyList(),
) {
    fun toMangaList(baseUrl: String): List<SManga> = items
        .mapNotNull { it.toSManga(baseUrl) }
        .distinctBy { it.url }
}

@Serializable
class LatestItemDto(
    private val series: MangaDto,
) {
    fun toSManga(baseUrl: String): SManga? = series.toSManga(baseUrl)
}

@Serializable
class MangaDto(
    private val title: String,
    private val slug: String,
    private val coverPath: String? = null,
    private val status: String? = null,
) {
    fun toSManga(baseUrl: String): SManga? {
        if (title.isEmpty() || slug.isEmpty()) return null

        return SManga.create().apply {
            title = this@MangaDto.title
            url = "/series/${this@MangaDto.slug}"
            thumbnail_url = coverPath?.toCoverUrl(baseUrl)
            status = this@MangaDto.status.toMangaStatus()
        }
    }
}

@Serializable
class ChapterListDto(
    private val chapters: List<ChapterDto> = emptyList(),
) {
    fun toSChapterList(dateFormat: SimpleDateFormat): List<SChapter> = chapters
        .mapNotNull { it.toSChapter(dateFormat) }
        .sortedByDescending { it.chapter_number }
}

@Serializable
class ChapterDto(
    private val id: Long,
    private val number: Double,
    private val title: String? = null,
    private val publishedAt: String? = null,
    private val isVipOnly: Boolean = false,
    private val unlockAt: String? = null,
    private val unlockAtMs: Long? = null,
) {
    fun toSChapter(dateFormat: SimpleDateFormat): SChapter? {
        if (id <= 0) return null

        val unlockDate = unlockDate(dateFormat)
        val isLocked = isVipOnly && !isUnlocked(unlockDate)
        val numberText = number.toString().removeSuffix(".0")
        return SChapter.create().apply {
            url = buildString {
                append("/api/viewer/bootstrap?c=")
                append(id)
                if (isLocked) {
                    append("&")
                    append(VIP_CHAPTER_QUERY)
                }
                if (isLocked && unlockDate > 0L) {
                    append("&")
                    append(VIP_UNLOCK_AT_QUERY)
                    append("=")
                    append(unlockDate)
                }
            }
            name = buildString {
                append("Capitulo ")
                append(numberText)
                title?.takeIf(String::isNotEmpty)?.let {
                    append(" - ")
                    append(it)
                }
                if (isLocked) {
                    append(" ")
                    append(LOCK_SYMBOL)
                }
            }
            chapter_number = number.toFloat()
            date_upload = dateFormat.tryParse(publishedAt.normalizedNextDate())
        }
    }

    private fun unlockDate(dateFormat: SimpleDateFormat): Long = unlockAtMs?.takeIf { it > 0L }
        ?: dateFormat.tryParse(unlockAt.normalizedNextDate())

    private fun isUnlocked(unlockDate: Long): Boolean = unlockDate > 0L && unlockDate <= System.currentTimeMillis()
}

@Serializable
class ViewerBootstrapDto(
    private val pages: List<ViewerPageDto> = emptyList(),
) {
    fun toPageList(baseUrl: String, referer: String): List<Page> = pages
        .sortedBy { it.index() }
        .mapIndexed { index, page ->
            Page(index, url = referer, imageUrl = page.imageUrl(baseUrl))
        }
}

@Serializable
class ViewerPageDto(
    @SerialName("i")
    private val index: Int,
    @SerialName("u")
    private val url: String,
) {
    fun index(): Int = index

    fun imageUrl(baseUrl: String): String = url.toAbsoluteUrl(baseUrl)
}

internal fun JsonElement.isCatalogGridPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return payload.string("className")?.contains("grid grid-cols-2") == true &&
        "children" in payload
}

internal fun JsonElement.isLatestPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return "items" in payload && "sevenDaysAgoMs" in payload
}

internal fun JsonElement.isSeriesDetailsPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return payload.string("className") == "container-main py-8" &&
        "children" in payload
}

internal fun JsonElement.isChapterListPayload(): Boolean {
    val payload = this as? JsonObject ?: return false
    return "chapters" in payload && "seriesId" in payload && "readChapterIds" in payload
}

internal fun JsonObject.toCatalogMangaList(baseUrl: String): List<SManga> = objects()
    .filter { "id" in it && "slug" in it && "title" in it && "coverPath" in it }
    .mapNotNull { it.toManga(baseUrl) }
    .distinctBy { it.url }
    .toList()

internal fun JsonObject.toDetailsManga(baseUrl: String, url: String): SManga {
    val cover = findCoverObject()
    return SManga.create().apply {
        this.url = url
        title = cover?.string("alt")
            ?: findObjectWithClass("text-2xl")?.childrenString()
            ?: error("Titulo da obra nao encontrado")
        thumbnail_url = cover?.string("src")?.toAbsoluteUrl(baseUrl)
        description = objects()
            .firstNotNullOfOrNull { it.string("description") }
            ?.takeIf(String::isNotEmpty)
        genre = objects()
            .filter { it.string("className")?.contains("rounded-full text-text-secondary") == true }
            .mapNotNull { it.childrenString() }
            .distinct()
            .joinToString()
            .takeIf(String::isNotEmpty)
        status = findStatusText().toMangaStatus()
        initialized = true
    }
}

private fun JsonObject.toManga(baseUrl: String): SManga? {
    val title = string("title") ?: return null
    val slug = string("slug") ?: return null
    if (title.isEmpty() || slug.isEmpty()) return null

    return SManga.create().apply {
        this.title = title
        url = "/series/$slug"
        thumbnail_url = string("coverPath")?.toCoverUrl(baseUrl)
        status = string("status").toMangaStatus()
    }
}

private fun JsonObject.findCoverObject(): JsonObject? = objects()
    .firstOrNull { it.string("src")?.contains("/api/cover/") == true && it.string("alt")?.isNotEmpty() == true }

private fun JsonObject.findObjectWithClass(classNamePart: String): JsonObject? = objects()
    .firstOrNull { it.string("className")?.contains(classNamePart) == true }

private fun JsonObject.findStatusText(): String? = objects()
    .mapNotNull { it.childrenString() }
    .firstOrNull { it == "Em andamento" || it == "Completo" || it == "Hiato" }

private fun JsonElement.objects(): Sequence<JsonObject> = sequence {
    when (this@objects) {
        is JsonObject -> {
            yield(this@objects)
            values.forEach { yieldAll(it.objects()) }
        }
        is JsonArray -> forEach { yieldAll(it.objects()) }
        else -> Unit
    }
}

private fun JsonObject.string(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull

private fun JsonObject.childrenString(): String? = string("children")
    ?: ((get("children") as? JsonPrimitive)?.contentOrNull)
    ?: ((get("children") as? JsonArray)?.singleOrNull() as? JsonPrimitive)?.contentOrNull

private fun String?.normalizedNextDate(): String? = this?.removePrefix("\$D")

private fun String?.toMangaStatus(): Int = when (this?.lowercase()) {
    "ongoing", "em andamento" -> SManga.ONGOING
    "completed", "complete", "completo", "finalizado" -> SManga.COMPLETED
    "hiatus", "hiato" -> SManga.ON_HIATUS
    else -> SManga.UNKNOWN
}

private fun String.toCoverUrl(baseUrl: String): String = "$baseUrl/api/cover/$this"

private fun String.toAbsoluteUrl(baseUrl: String): String = when {
    startsWith("http://") || startsWith("https://") -> this
    startsWith("//") -> "https:$this"
    startsWith("/") -> "$baseUrl$this"
    else -> "$baseUrl/$this"
}
