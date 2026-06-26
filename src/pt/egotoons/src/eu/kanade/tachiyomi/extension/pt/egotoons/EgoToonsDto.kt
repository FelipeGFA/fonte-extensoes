package eu.kanade.tachiyomi.extension.pt.egotoons

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

@Serializable
data class EgoToonsResponseDto<T>(
    val sucesso: Boolean,
    val pagination: EgoToonsPaginationDto? = null,
    val obras: List<T> = emptyList(),
    val obra: T? = null,
    val capitulo: EgoToonsPageListDto? = null,
)

@Serializable
data class EgoToonsMangaDto(
    val id: Int,
    val nome: String? = null,
    val title: String? = null,
    val status_nome: String? = null,
    val descricao: String? = null,
    val imagem: String? = null,
    val coverImage: String? = null,
    val tags: List<TagDto> = emptyList(),
    val capitulos: List<EgoToonsChapterDto> = emptyList(),
) {
    @Serializable
    data class TagDto(
        val id: Int = 0,
        val nome: String,
    )

    fun toSManga(): SManga = SManga.create().apply {
        title = this@EgoToonsMangaDto.nome ?: this@EgoToonsMangaDto.title ?: ""
        val imgUrl = imagem ?: coverImage
        thumbnail_url = if (imgUrl != null && !imgUrl.startsWith("http")) {
            if (imgUrl.startsWith("/")) "https://cdn.egotoons.com$imgUrl" else "https://cdn.egotoons.com/$imgUrl"
        } else {
            imgUrl
        }
        url = "obra/$id"
        description = descricao?.let { Jsoup.parseBodyFragment(it, "https://egotoons.com").text() }
        genre = tags.joinToString { it.nome }
        status = when (this@EgoToonsMangaDto.status_nome) {
            "Ativo", "Em Andamento" -> SManga.ONGOING
            "Hiato", "Pausado" -> SManga.ON_HIATUS
            "Completo", "Finalizado" -> SManga.COMPLETED
            "Cancelado" -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }
    }
}

@Serializable
data class EgoToonsPaginationDto(
    val pagina: Int,
    val limite: Int,
    val total: Int,
    val totalPaginas: Int,
    val hasNextPage: Boolean,
)

@Serializable
data class EgoToonsChapterDto(
    val id: Int,
    val obra_id: Int,
    val numero: String,
    val nome: String? = null,
    val data_cadastro: String? = null,
    val criado_em: String? = null,
) {
    fun toSChapter(): SChapter = SChapter.create().apply {
        val parsedNum = numero.toFloatOrNull() ?: -1f
        val chapterNumStr = if (parsedNum % 1 == 0f) parsedNum.toInt().toString() else parsedNum.toString()
        val chapterName = "Capítulo $chapterNumStr"

        name = when {
            nome.isNullOrBlank() -> chapterName
            nome.contains(chapterNumStr, ignoreCase = true) -> nome
            else -> "$chapterName - $nome"
        }
        chapter_number = parsedNum
        url = "obra/$obra_id/capitulo/$chapterNumStr"
        date_upload = DATE_FORMATTER.tryParse(data_cadastro ?: criado_em)
    }
}

@Serializable
data class EgoToonsPageListDto(
    val paginas: List<EgoToonsPageDto> = emptyList(),
)

@Serializable
data class EgoToonsPageDto(
    val numero: Int,
    val url: String,
)
