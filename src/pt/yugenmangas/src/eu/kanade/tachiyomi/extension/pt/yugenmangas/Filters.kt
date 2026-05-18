package eu.kanade.tachiyomi.extension.pt.yugenmangas

import eu.kanade.tachiyomi.source.model.Filter

class StatusFilter : Filter.Select<String>("Status", statusOptions.map { it.first }.toTypedArray()) {
    val selectedValue: String
        get() = statusOptions[state].second
}

class SortFilter :
    Filter.Sort(
        "Ordenar por",
        sortOptions.map { it.first }.toTypedArray(),
        Selection(DEFAULT_SORT_INDEX, false),
    ) {
    val selectedValue: String
        get() = sortOptions[state?.index ?: DEFAULT_SORT_INDEX].second

    val selectedOrder: String
        get() = if (state?.ascending == true) "asc" else "desc"
}

class GenreFilter :
    Filter.Group<Genre>(
        "Gêneros",
        genreOptions.map { Genre(it.first, it.second) },
    ) {
    val includedIds: String
        get() = selectedIds(Filter.TriState.STATE_INCLUDE)

    val excludedIds: String
        get() = selectedIds(Filter.TriState.STATE_EXCLUDE)

    private fun selectedIds(state: Int): String = this.state
        .filter { it.state == state }
        .joinToString(",") { it.id }
}

class Genre(val id: String, name: String) : Filter.TriState(name)

private const val DEFAULT_SORT_INDEX = 3

private val statusOptions = listOf(
    "Todos os status" to "",
    "Em Andamento" to "ongoing",
    "Finalizada" to "completed",
    "Hiato" to "hiatus",
    "Cancelada" to "canceled",
    "Abandonada" to "dropped",
)

private val sortOptions = listOf(
    "Nome" to "name",
    "Data de Criação" to "created_at",
    "Avaliação" to "rating",
    "Visualizações" to "total_views",
)

private val genreOptions = listOf(
    "e954f1f9-393e-4162-af7b-5e388170148b" to "Ação",
    "0bed9640-7a10-4500-83fe-494a94ed3ea3" to "Aventura",
    "f5fdcfca-a61e-4873-8e7f-5667c15626e6" to "Artes Marciais",
    "177e4ead-aa20-44f5-9e39-88bce388a417" to "Fantasia",
    "b207936e-f92e-4a41-b65e-9824f086e36a" to "Superpoderes",
    "53161d76-1a48-4f56-a058-7aacb87e32e8" to "Sobrenatural",
    "33eaf086-93e8-4cd5-a3e3-77cf23199687" to "Sci-Fi",
    "5c4019b2-1768-4c23-a3ee-76ac42a0a71b" to "Apocalíptico",
    "c511d329-0f2f-4a4e-afde-59a2ba4d9a7d" to "Guerra",
    "d2f22988-9df9-47a5-b7c6-7467aa476de6" to "Reencarnação",
    "875e5a87-b7a6-4756-9a1d-c0cbffac3ffc" to "Isekai",
    "011b2218-d2cf-4e7a-b7f4-8844f786d420" to "Cultivação",
    "4654f65b-5836-47c3-b534-ed1d7b97e38d" to "Magia",
    "e4ecea1c-4b52-4926-8712-b93b18912ced" to "Histórico",
    "1f22c2f9-2a01-49b7-87e3-bbbca158ef87" to "Drama",
    "e37d3738-428e-4280-8675-cac70e62f516" to "Tragédia",
    "482278f7-7b97-4089-acc8-ddffc039d946" to "Romance",
    "93b6caea-b6e5-442a-a601-e701a413f00b" to "Slice of Life",
    "d11ff009-f781-40b4-9e3f-7f48e0f16149" to "Psicologia",
    "73f4bbd5-12d8-466d-bb3e-8358ab5bd339" to "Mistério",
    "fb05bac2-d4d3-4bea-9696-2b60daf91f7a" to "Thriller",
    "1e327270-55dc-4a07-bfd7-3cdbed93a91a" to "Suspense",
    "07dfde02-68ee-4c06-92a3-6e76d9ef8374" to "Crime / Policial",
    "9878e6ef-2e46-456a-aec0-3457a959211c" to "Comédia",
    "2617a39a-7771-47c6-b17c-a7faed59202a" to "Escolar",
    "a66ab027-8d20-4c19-be07-8f004ff291a4" to "Shoujo",
    "deffdc5c-a02f-40f9-8d86-02f426e9458f" to "Shounen",
    "c4683aa8-0ee9-4042-988b-93b2354ce468" to "Seinen",
    "299707e4-dbf4-4bab-b346-31f3c802f00a" to "Terror",
    "f52e876a-c624-46ba-a633-c0413344bcf1" to "Gore",
    "ebadb4a7-946b-43fe-a226-cda49ceab990" to "Adulto",
    "514a0d89-5da5-45d1-ba2b-15c5600532c7" to "Máfia",
    "da9bbbfb-f83d-4e59-bf1e-e9e6f0abf250" to "Sobrevivência",
    "84c7c9e9-15aa-4c58-8542-7b1395ae8905" to "Yuri",
    "c26631ee-1d73-464f-9b7f-66eb38293e07" to "Yaoi",
    "84154052-78a6-4e88-955c-8c19e69003db" to "Harém",
    "197a2ff5-fb44-4b1e-8a62-3b765a3d78ba" to "Josei",
    "ff141f84-97b7-436d-bb65-3cdd10ce550c" to "Estratégia",
    "140639fb-9439-4ae5-b1a9-b86dee5f15d3" to "Cultivo",
    "9d66670d-af52-4a30-8082-bdac41735d86" to "Murim",
    "95a0afd5-37ee-402d-8074-bfc38b85e201" to "Culinária",
    "1ac02b9f-55bc-4e1a-998d-84bc4bd4c604" to "Esportes",
    "accb87d7-6912-408e-a280-e0a8d0ebfeab" to "Idol",
    "ab16a24a-3efc-4fd7-8801-10b9c0e1a1ce" to "Música",
    "d9911173-fe5d-472e-b1ff-37177a838e02" to "Vingança",
)
