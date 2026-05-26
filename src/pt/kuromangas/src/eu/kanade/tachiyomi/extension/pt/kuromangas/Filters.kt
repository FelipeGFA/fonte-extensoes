package eu.kanade.tachiyomi.extension.pt.kuromangas

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList

fun getFilters() = FilterList(
    SortFilter("Ordenar por", SORT_OPTIONS),
)

class SortFilter(displayName: String, private val sortOptions: List<Pair<String, String>>) : Filter.Select<String>(displayName, sortOptions.map { it.first }.toTypedArray()) {
    val selectedSort: String
        get() = sortOptions[state].second
    val selectedOrder: String
        get() = if (state in ASCENDING_SORT_OPTIONS) "ASC" else "DESC"
}

private val SORT_OPTIONS = listOf(
    "Mais Populares" to "view_count",
    "Melhor Avaliados" to "average_rating",
    "Mais Antigos" to "created_at",
    "Titulo (A-Z)" to "title",
    "Titulo (Z-A)" to "title",
)

private val ASCENDING_SORT_OPTIONS = 2..3
