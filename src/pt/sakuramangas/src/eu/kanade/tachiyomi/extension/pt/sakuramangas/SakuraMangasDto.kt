package eu.kanade.tachiyomi.extension.pt.sakuramangas

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl

@Serializable
class SakuraMangasLatestDto(
    private val titulo: String,
    private val url_manga: String,
    private val thumb: String,
) {
    fun toSManga(baseUri: String) = SManga.create().apply {
        title = titulo
        thumbnail_url = "$baseUri$thumb"
        url = url_manga
    }
}

@Serializable
class SakuraMangasPopularResponseDto(val data: List<SakuraMangasPopularDto>)

@Serializable
class SakuraMangasPopularDto(
    private val titulo: String,
    private val url: String,
    private val thumb: String,
) {
    fun toSManga(baseUri: String) = SManga.create().apply {
        title = titulo
        thumbnail_url = "$baseUri$thumb"
        url = this@SakuraMangasPopularDto.url
    }
}

@Serializable
class SakuraMangasSearchResponseDto(val data: List<SakuraMangasSearchDto>, val hasMore: Boolean)

@Serializable
class SakuraMangasSearchDto(
    private val titulo: String,
    private val url_slug: String,
    private val sinopse: String? = null,
) {
    fun toSManga(baseUri: String) = SManga.create().apply {
        title = titulo
        thumbnail_url = "$baseUri/obras/$url_slug/thumb_256.jpg"
        url = "/obras/$url_slug"
        description = sinopse
    }
}

@Serializable
class SakuraMangaInfoDto(
    private val titulo: String,
    private val autor: String?,
    private val sinopse: String?,
    private val tags: List<String>,
    private val demografia: String?,
    private val status: String,
    private val ano: Int?,
    private val classificacao: String?,
    private val avaliacao: Double?,
) {
    fun toSManga(mangaUrl: String) = SManga.create().apply {
        title = titulo
        author = autor
        genre = tags.joinToString()
        status = when (this@SakuraMangaInfoDto.status) {
            "concluído" -> SManga.COMPLETED
            "em andamento" -> SManga.ONGOING
            else -> SManga.UNKNOWN
        }
        description = buildString {
            sinopse?.takeIf { it.isNotBlank() }?.let { appendLine(it); appendLine() }
            ano?.let { appendLine("Ano: $it") }
            demografia?.takeIf { it.isNotBlank() }?.let { appendLine("Demografia: $it") }
            classificacao?.takeIf { it.isNotBlank() }?.let { appendLine("Classificação: $it") }
            avaliacao?.let { appendLine("Avaliação: $it") }
        }.trim()
        thumbnail_url = "${mangaUrl.trimEnd('/')}/thumb_256.jpg"
        url = mangaUrl.toHttpUrl().encodedPath
        initialized = true
    }
}

@Serializable
class SakuraMangasChaptersDto(val has_more: Boolean, val data: List<SakuraMangasChapterGroupDto>)

@Serializable
class SakuraMangasChapterGroupDto(
    val numero: Float,
    val data_timestamp: Long,
    val versoes: List<SakuraMangasChapterVersionDto>,
) {
    fun toSChapter() = SChapter.create().apply {
        val first = versoes.first()
        val num = if (numero.rem(1f) == 0f) numero.toInt().toString() else numero.toString()
        url = first.url
        name = "Cap. $num${first.titulo?.let { " - $it" } ?: ""}"
        date_upload = data_timestamp * 1000
        chapter_number = numero
        scanlator = first.scans.joinToString { it.nome }
    }
}

@Serializable
class SakuraMangasChapterVersionDto(val titulo: String?, val url: String, val scans: List<SakuraMangasChapterScanDto>)

@Serializable
class SakuraMangasChapterScanDto(val nome: String)
