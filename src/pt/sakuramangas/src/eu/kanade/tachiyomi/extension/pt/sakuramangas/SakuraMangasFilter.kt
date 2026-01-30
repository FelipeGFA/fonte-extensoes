package eu.kanade.tachiyomi.extension.pt.sakuramangas

import eu.kanade.tachiyomi.source.model.Filter

class GenreList(title: String, genres: Array<Genre>) :
    Filter.Group<GenreCheckBox>(title, genres.map { GenreCheckBox(it.name, it.id) })

class GenreCheckBox(name: String, val id: String = name) : Filter.TriState(name)

data class Genre(val name: String, val id: String)

open class SelectFilter(name: String, private val options: List<Pair<String, String>>) :
    Filter.Select<String>(name, options.map { it.first }.toTypedArray()) {
    fun getValue() = options.getOrNull(state)?.second.orEmpty()
}

class DemographyFilter(name: String, options: List<Pair<String, String>>) : SelectFilter(name, options)
class ClassificationFilter(name: String, options: List<Pair<String, String>>) : SelectFilter(name, options)
class OrderByFilter(name: String, options: List<Pair<String, String>>) : SelectFilter(name, options)
