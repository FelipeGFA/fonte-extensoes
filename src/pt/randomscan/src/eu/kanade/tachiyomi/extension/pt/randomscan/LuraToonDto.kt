package eu.kanade.tachiyomi.extension.pt.randomscan

import eu.kanade.tachiyomi.extension.pt.randomscan.LuraToon.Companion.DATE_FORMAT
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Main page response (popular/latest)
@Serializable
data class MainPageDto(
    val slides: List<SlideDto> = emptyList(),
    val lancamentos: List<MangaItemDto> = emptyList(),
    @SerialName("top_10")
    val top10: List<TopMangaDto> = emptyList(),
)

@Serializable
data class SlideDto(
    val id: Int,
    val title: String,
    val capa: String,
    val slug: String,
)

@Serializable
data class TopMangaDto(
    val id: Int,
    val title: String,
    val capa: String,
    val slug: String,
    val tipo: String? = null,
) {
    fun toSManga(baseUrl: String) = SManga.create().apply {
        url = "/$slug/"
        title = this@TopMangaDto.title
        thumbnail_url = "$baseUrl$capa"
    }
}

@Serializable
data class MangaItemDto(
    val id: Int,
    val title: String,
    val capa: String,
    val slug: String,
    val caps: List<ChapterItemDto> = emptyList(),
) {
    fun toSManga(baseUrl: String) = SManga.create().apply {
        url = "/$slug/"
        title = this@MangaItemDto.title
        thumbnail_url = "$baseUrl$capa"
    }
}

@Serializable
data class ChapterItemDto(
    val date: String,
    val num: Float,
    val slug: String,
)

// Search response
@Serializable
data class SearchResultDto(
    val obras: List<SearchMangaDto> = emptyList(),
)

@Serializable
data class SearchMangaDto(
    val id: Int,
    val slug: String,
    val titulo: String,
    val capa: String,
) {
    fun toSManga(baseUrl: String) = SManga.create().apply {
        url = "/$slug/"
        title = this@SearchMangaDto.titulo
        thumbnail_url = "$baseUrl$capa"
    }
}

// Manga details response
@Serializable
data class MangaDetailsDto(
    val type: String? = null,
    val id: Int,
    val capa: String,
    val titulo: String,
    val alternativo: String? = null,
    val autor: String? = null,
    val artista: String? = null,
    val estudio: String? = null,
    val status: String? = null,
    val sinopse: String? = null,
    val ano: Int? = null,
    val rank: Int? = null,
    val dia: String? = null,
    val tipo: String? = null,
    val generos: List<GenreDto> = emptyList(),
    val caps: List<ChapterDto> = emptyList(),
    @SerialName("slug")
    private val _slug: String? = null,
) {
    val slug: String
        get() = _slug ?: titulo.toSlug()

    fun toSManga(baseUrl: String) = SManga.create().apply {
        url = "/${this@MangaDetailsDto.slug}/"
        title = titulo
        thumbnail_url = "$baseUrl$capa"
        description = buildString {
            sinopse?.let { append(it) }
            alternativo?.takeIf { it.isNotBlank() }?.let {
                if (isNotEmpty()) append("\n\n")
                append("Títulos alternativos: $it")
            }
            ano?.let {
                if (isNotEmpty()) append("\n")
                append("Ano: $it")
            }
        }
        author = this@MangaDetailsDto.autor?.takeIf { it.isNotBlank() }
        artist = this@MangaDetailsDto.artista?.takeIf { it.isNotBlank() }
        genre = buildList {
            tipo?.let { add(it) }
            addAll(generos.map { it.name })
        }.joinToString()
        status = when (this@MangaDetailsDto.status?.lowercase()) {
            "em lançamento", "em andamento", "ongoing" -> SManga.ONGOING
            "completo", "completed" -> SManga.COMPLETED
            "hiatus" -> SManga.ON_HIATUS
            "cancelado", "cancelled" -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }
        initialized = true
    }

    private fun String.toSlug(): String {
        return this.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
    }
}

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)

@Serializable
data class ChapterDto(
    val num: Float,
    val data: String,
    val slug: String,
    val id: Int,
) {
    fun toSChapter(mangaSlug: String) = SChapter.create().apply {
        url = "/$mangaSlug/$slug/"
        name = "Capítulo ${num.formatNumber()}"
        chapter_number = this@ChapterDto.num
        date_upload = DATE_FORMAT.tryParse(data)
    }

    private fun Float.formatNumber(): String {
        return if (this % 1 == 0f) this.toInt().toString() else this.toString()
    }
}

// Chapter pages response
@Serializable
data class ChapterPagesDto(
    val pages: List<String> = emptyList(),
    val titulo: String? = null,
    val files: Int? = null,
    val obra: ChapterObraDto? = null,
    @SerialName("next_cap")
    val nextCap: String? = null,
    @SerialName("before_cap")
    val beforeCap: String? = null,
)

@Serializable
data class ChapterObraDto(
    val id: Int,
    val titulo: String,
)
