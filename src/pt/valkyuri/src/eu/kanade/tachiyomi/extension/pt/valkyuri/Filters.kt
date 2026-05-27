package eu.kanade.tachiyomi.extension.pt.valkyuri

import eu.kanade.tachiyomi.source.model.Filter

class SectionFilter :
    Filter.Select<String>(
        "Seção",
        SECTION_OPTIONS.map { it.title }.toTypedArray(),
        SECTION_OPTIONS.indexOfFirst { it.key == DEFAULT_SECTION },
    ) {
    val selected: String
        get() = SECTION_OPTIONS[state].key
}

class CatalogSortFilter :
    Filter.Select<String>(
        "Ordenação do catálogo",
        CATALOG_SORT_OPTIONS.map { it.title }.toTypedArray(),
    ) {
    val selected: String
        get() = CATALOG_SORT_OPTIONS[state].key
}

private class SectionOption(
    val title: String,
    val key: String,
)

private class CatalogSortOption(
    val title: String,
    val key: String,
)

const val CATALOG_SECTION = "catalog"

private const val DEFAULT_SECTION = "popular_week"

private val SECTION_OPTIONS = arrayOf(
    SectionOption("Catálogo completo", CATALOG_SECTION),
    SectionOption("Em alta agora", "trending_now"),
    SectionOption("Populares da semana", "popular_week"),
    SectionOption("Lançamentos da Valkirye", "new_releases"),
    SectionOption("Comece por aqui", "start_here"),
    SectionOption("Curtinhos para maratonar", "short_binge"),
    SectionOption("Obras longas para maratonar", "long_binge"),
    SectionOption("Obras completas", "completed"),
    SectionOption("Romance em alta", "romance"),
    SectionOption("GL/Yuri em destaque", "gl_yuri"),
    SectionOption("Drama pesado", "heavy_drama"),
    SectionOption("Comédia leve", "light_comedy"),
    SectionOption("Ação e aventura", "action_adventure"),
    SectionOption("Fantasia e sobrenatural", "fantasy_supernatural"),
    SectionOption("Joias escondidas", "hidden_gems"),
    SectionOption("Escolha da staff", "staff_picks"),
    SectionOption("Mais seguidas", "most_followed"),
)

private val CATALOG_SORT_OPTIONS = arrayOf(
    CatalogSortOption("Mais recentes", "latest"),
    CatalogSortOption("Mais populares", "popular"),
    CatalogSortOption("A-Z", "name"),
    CatalogSortOption("Atualizados", "updated"),
)
