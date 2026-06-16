package eu.kanade.tachiyomi.extension.pt.risentoons

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList

internal fun getFilters(): FilterList = FilterList(
    TypeFilter(),
    StatusFilter(),
    GenreFilter(),
)

internal class TypeFilter :
    UriSelectFilter(
        "Tipo",
        listOf(
            "Todos" to null,
            "Manga" to "manga",
            "Manhua" to "manhua",
            "Manhwa" to "manhwa",
        ),
    )

internal class StatusFilter :
    UriSelectFilter(
        "Status",
        listOf(
            "Todos" to null,
            "Em andamento" to "ongoing",
            "Completo" to "completed",
            "Hiato" to "hiatus",
        ),
    )

internal class GenreFilter :
    UriSelectFilter(
        "Genero",
        listOf(
            "Todos" to null,
            "AÇÃO" to "AÇÃO",
            "ARTES MARCIAIS" to "ARTES MARCIAIS",
            "AVENTURA" to "AVENTURA",
            "COMÉDIA" to "COMÉDIA",
            "CRIME" to "CRIME",
            "DEMÔNIOS" to "DEMÔNIOS",
            "DRAMA" to "DRAMA",
            "ECCHI" to "ECCHI",
            "ERÓTICA" to "ERÓTICA",
            "ESCOLAR" to "ESCOLAR",
            "ESPORTES" to "ESPORTES",
            "ESTRATÉGIA" to "ESTRATÉGIA",
            "FANTASIA" to "FANTASIA",
            "Filme de ação" to "Filme de ação",
            "GAROTAS MONSTROS" to "GAROTAS MONSTROS",
            "HARÉM" to "HARÉM",
            "HORROR" to "HORROR",
            "ISEKAI" to "ISEKAI",
            "MÁGIA" to "MÁGIA",
            "MAGIA ESCOLAR" to "MAGIA ESCOLAR",
            "MILITAR" to "MILITAR",
            "MISTÉRIO" to "MISTÉRIO",
            "MONSTROS" to "MONSTROS",
            "PSICOLÓGICO" to "PSICOLÓGICO",
            "REENCARNAÇÃO" to "REENCARNAÇÃO",
            "ROMANCE" to "ROMANCE",
            "SCI-FI" to "SCI-FI",
            "SEINEN" to "SEINEN",
            "SHOUNEN" to "SHOUNEN",
            "SLICE OF LIFE" to "SLICE OF LIFE",
            "SOBRENATURAL" to "SOBRENATURAL",
            "SOBREVIVÊNCIA" to "SOBREVIVÊNCIA",
            "SUSPENSE" to "SUSPENSE",
            "TELA DE SISTEMA" to "TELA DE SISTEMA",
            "TRAGÉDIA" to "TRAGÉDIA",
            "TROCA DE GÊNERO" to "TROCA DE GÊNERO",
            "VIAGEM NO TEMPO" to "VIAGEM NO TEMPO",
            "VIDA ESCOLAR" to "VIDA ESCOLAR",
            "VIDEO GAME" to "VIDEO GAME",
            "ZOMBIE" to "ZOMBIE",
        ),
    )

internal open class UriSelectFilter(
    name: String,
    private val options: List<Pair<String, String?>>,
) : Filter.Select<String>(
    name,
    options.map { it.first }.toTypedArray(),
) {
    val selected: String?
        get() = options[state].second
}
