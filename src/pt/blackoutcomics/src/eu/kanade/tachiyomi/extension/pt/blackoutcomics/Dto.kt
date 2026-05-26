package eu.kanade.tachiyomi.extension.pt.blackoutcomics

import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SearchResponse(
    private val items: List<SearchItem> = emptyList(),
) {
    fun toSMangaList(baseUrl: String): List<SManga> = items.map { it.toSManga(baseUrl) }
}

@Serializable
class SearchItem(
    @SerialName("PJT_ID") private val id: Int,
    @SerialName("PJT_NAME") private val name: String,
    @SerialName("PJT_IMG_PR") private val imgPr: String? = null,
    @SerialName("PJT_IMG_PR_URL") private val imgUrl: String? = null,
) {
    fun toSManga(baseUrl: String): SManga = SManga.create().apply {
        title = name
        url = "/comics/$id"
        thumbnail_url = imgUrl ?: imgPr?.let { "$baseUrl/$it" }
    }
}

@Serializable
class LoginResponse(
    private val success: String? = null,
) {
    fun isSuccess() = success != null
}
