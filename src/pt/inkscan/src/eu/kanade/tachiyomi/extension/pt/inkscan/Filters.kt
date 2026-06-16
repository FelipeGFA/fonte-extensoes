package eu.kanade.tachiyomi.extension.pt.inkscan

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import keiyoushi.utils.firstInstanceOrNull

internal class SortFilter :
    ChoiceFilter(
        "Ordenar por",
        arrayOf(
            "total_views.desc" to "Mais populares",
            "created_at.desc" to "Mais recentes",
            "total_curtidas.desc" to "Mais curtidas",
            "titulo.asc" to "A-Z",
        ),
    )

internal class FormatFilter :
    ChoiceFilter(
        "Formato",
        arrayOf(
            "" to "Todos",
            "manga" to "Manga",
            "manhwa" to "Manhwa",
            "manhua" to "Manhua",
            "comic" to "Comic",
            "novel" to "Novel",
            "shoujo" to "Shoujo",
        ),
    )

internal class StatusFilter :
    ChoiceFilter(
        "Status",
        arrayOf(
            "" to "Todos",
            "ongoing" to "Em andamento",
            "completed" to "Completo",
            "hiatus" to "Hiato",
            "cancelled" to "Cancelado",
        ),
    )

internal class ChapterCountFilter :
    ChoiceFilter(
        "Capitulos",
        arrayOf(
            "" to "Todos",
            "1..10" to "1-10",
            "11..50" to "11-50",
            "51..100" to "51-100",
            "101.." to "100+",
        ),
    )

internal open class ChoiceFilter(
    name: String,
    private val entries: Array<Pair<String, String>>,
) : Filter.Select<String>(
    name,
    entries.map { it.second }.toTypedArray(),
) {
    fun selectedValue(): String = entries[state].first
}

internal class TagFilter :
    Filter.Group<TagCheckBox>(
        "Generos/Tags",
        listOf(
            TagCheckBox("Acao", "A\u00e7\u00e3o"),
            TagCheckBox("Aventura"),
            TagCheckBox("Artes Marciais"),
            TagCheckBox("Colorido"),
            TagCheckBox("Comedia", "Com\u00e9dia"),
            TagCheckBox("Drama"),
            TagCheckBox("Fantasia"),
            TagCheckBox("Harem", "Har\u00e9m"),
            TagCheckBox("Historico", "Hist\u00f3rico"),
            TagCheckBox("Isekai"),
            TagCheckBox("Josei"),
            TagCheckBox("Magia"),
            TagCheckBox("Misterio", "Mist\u00e9rio"),
            TagCheckBox("Reencarnacao", "Reencarna\u00e7\u00e3o"),
            TagCheckBox("Regressao", "Regress\u00e3o"),
            TagCheckBox("Romance"),
            TagCheckBox("Seinen"),
            TagCheckBox("Shoujo"),
            TagCheckBox("Shounen"),
            TagCheckBox("Sistema"),
            TagCheckBox("Sobrenatural"),
            TagCheckBox("Vida Escolar"),
            TagCheckBox("Webtoon"),
        ),
    )

internal class TagCheckBox(
    name: String,
    val value: String = name,
) : Filter.CheckBox(name)

internal fun getFilters(): FilterList = FilterList(
    SortFilter(),
    FormatFilter(),
    StatusFilter(),
    ChapterCountFilter(),
    TagFilter(),
)

internal fun FilterList.sortValue(): String = firstInstanceOrNull<SortFilter>()?.selectedValue().orEmpty()

internal fun FilterList.formatValue(): String = firstInstanceOrNull<FormatFilter>()?.selectedValue().orEmpty()

internal fun FilterList.statusValue(): String = firstInstanceOrNull<StatusFilter>()?.selectedValue().orEmpty()

internal fun FilterList.chapterRange(): Pair<Int?, Int?>? = when (firstInstanceOrNull<ChapterCountFilter>()?.selectedValue()) {
    "1..10" -> 1 to 10
    "11..50" -> 11 to 50
    "51..100" -> 51 to 100
    "101.." -> 101 to null
    else -> null
}

internal fun FilterList.selectedTags(): List<String> = firstInstanceOrNull<TagFilter>()?.state
    ?.filter(TagCheckBox::state)
    ?.map(TagCheckBox::value)
    .orEmpty()
