package eu.kanade.tachiyomi.extension.pt.egotoons

import eu.kanade.tachiyomi.source.model.Filter

open class UriPartFilter(name: String, val vals: Array<Pair<String, String>>) : Filter.Select<String>(name, vals.map { it.first }.toTypedArray()) {
    fun toUriPart() = vals[state].second
}

class FormatFilter : UriPartFilter("Formatos", formatosList)
class StatusFilter : UriPartFilter("Status", statusList)

class TagCheckBox(name: String, val id: String) : Filter.CheckBox(name)
class TagFilter : Filter.Group<TagCheckBox>("Tags", tagsList.map { TagCheckBox(it.first, it.second) })

private val formatosList = arrayOf(
    Pair("Todos", ""),
    Pair("Comic", "20"),
    Pair("Manga", "24"),
    Pair("Manhua", "17"),
    Pair("Manhwa", "25"),
    Pair("Novel", "18"),
    Pair("Shoujo", "19"),
    Pair("Webtoon", "26"),
)

private val statusList = arrayOf(
    Pair("Todos", ""),
    Pair("Ativo", "6"),
    Pair("Cancelado", "4"),
    Pair("Em Andamento", "1"),
    Pair("Finalizado", "3"),
    Pair("Hiato", "5"),
    Pair("Pausado", "2"),
)

private val tagsList = arrayOf(
    Pair("+18", "48"),
    Pair("Ação", "2"),
    Pair("Adulto", "64"),
    Pair("Alienígenas", "65"),
    Pair("Apocalipse", "33"),
    Pair("Apocalíptico", "58"),
    Pair("Artes Marciais", "24"),
    Pair("Aventura", "3"),
    Pair("Bullying", "60"),
    Pair("Comédia", "6"),
    Pair("Crime", "51"),
    Pair("Culinaria", "28"),
    Pair("Cultivo", "23"),
    Pair("Demônios ", "39"),
    Pair("Drama", "7"),
    Pair("Dungeon", "25"),
    Pair("Ecchi", "42"),
    Pair("Escolar", "56"),
    Pair("Esportes", "38"),
    Pair("Fantasia", "4"),
    Pair("Fatia da Vida/Slice of Life", "41"),
    Pair("Ficção Científica", "40"),
    Pair("Finalizado", "55"),
    Pair("Gore", "59"),
    Pair("Harém", "44"),
    Pair("Histórico", "11"),
    Pair("Horror", "9"),
    Pair("Isekai", "19"),
    Pair("Jogo", "46"),
    Pair("Jogos", "66"),
    Pair("Josei", "17"),
    Pair("Luta", "35"),
    Pair("máfia", "36"),
    Pair("Magia", "29"),
    Pair("manhua", "45"),
    Pair("Manhwa", "67"),
    Pair("Militar", "14"),
    Pair("Mistério", "43"),
    Pair("Moderno", "54"),
    Pair("Monstros", "37"),
    Pair("Murim", "31"),
    Pair("Necromante", "32"),
    Pair("One-shot", "18"),
    Pair("Oneshot", "49"),
    Pair("Policial", "52"),
    Pair("Pós Apocalíptico", "68"),
    Pair("Psicológico", "27"),
    Pair("Reencarnação", "21"),
    Pair("Regressão", "47"),
    Pair("Retorno", "20"),
    Pair("Romance", "5"),
    Pair("Sci-Fi", "62"),
    Pair("Seinen", "34"),
    Pair("Shoujo", "16"),
    Pair("Shounen", "15"),
    Pair("Shounen Ai", "63"),
    Pair("Sistema", "22"),
    Pair("Slice of Life", "61"),
    Pair("Sobrenatural", "13"),
    Pair("SuperPoder", "30"),
    Pair("Super Poderes", "57"),
    Pair("Suspense", "10"),
    Pair("Terror", "8"),
    Pair("Tragédia", "26"),
    Pair("Viagem no Tempo", "53"),
    Pair("Vida escolar", "12"),
    Pair("Yuri", "50"),
)
