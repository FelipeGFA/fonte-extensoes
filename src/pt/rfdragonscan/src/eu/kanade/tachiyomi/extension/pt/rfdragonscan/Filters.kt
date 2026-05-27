package eu.kanade.tachiyomi.extension.pt.rfdragonscan

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList

internal class GenreFilter :
    ProjectSelectFilter(
        "Gênero",
        "genero",
        GENRES,
    )

internal class StatusFilter :
    ProjectSelectFilter(
        "Status",
        "status",
        STATUSES,
    )

internal class TypeFilter :
    ProjectSelectFilter(
        "Tipo",
        "flag",
        TYPES,
    )

internal open class ProjectSelectFilter(
    name: String,
    private val filter: String,
    private val entries: Array<Pair<String, String>>,
) : Filter.Select<String>(
    name,
    entries.map { it.first }.toTypedArray(),
) {
    fun selectedFilter(): ProjectFilterOption? {
        val term = entries[state].second
        if (term.isEmpty()) return null

        return ProjectFilterOption(filter, term)
    }
}

internal class ProjectFilterOption(
    val filter: String,
    val term: String,
)

internal fun getFilters(): FilterList = FilterList(
    GenreFilter(),
    StatusFilter(),
    TypeFilter(),
)

private val GENRES = arrayOf(
    Pair("Todos", ""),
    Pair("Ação", "Ação"),
    Pair("Artes Marciais", "Artes Marciais"),
    Pair("Aventura", "Aventura"),
    Pair("Comédia", "Comédia"),
    Pair("Drama", "Drama"),
    Pair("Ecchi", "Ecchi"),
    Pair("Escolar", "Escolar"),
    Pair("Fantasia", "Fantasia"),
    Pair("Harém", "Harém"),
    Pair("Isekai", "Isekai"),
    Pair("Magia", "Magia"),
    Pair("Reencarnação", "Reencarnação"),
    Pair("Romance", "Romance"),
    Pair("Seinen", "Seinen"),
    Pair("Shounen", "Shounen"),
    Pair("Sobrenatural", "Sobrenatural"),
    Pair("Vingança", "Vingança"),
)

private val STATUSES = arrayOf(
    Pair("Todos", ""),
    Pair("Ativo", "ACTIVE"),
    Pair("Finalizado", "FINISHED"),
    Pair("Em dia", "UP_TO_DATE"),
)

private val TYPES = arrayOf(
    Pair("Todos", ""),
    Pair("Manhwa Chinês", "🇨🇳"),
    Pair("Manhwa Coreano", "🇰🇷"),
)
