package eu.kanade.tachiyomi.extension.pt.sakuramangas

/**
 * Security keys extracted from obfuscated scripts.
 */
data class SecurityKeys(
    val mangaInfo: Long,
    val chapterRead: Long,
    val xVerificationKey1: String,
    val xVerificationKey2: String,
    val clientSignature: String,
)

/**
 * Extracts security keys from the site's obfuscated scripts.
 * Values are in plain text within 'c':[] arrays in the code.
 */
object SecurityKeysExtractor {

    // Client signature format: XXXXX-XXXXX-XXXXXX
    private val signatureRegex = """['"](\w{5}-\w{5}-\w{6})['"]""".toRegex()

    // UUID formats found in site scripts:
    // Format 1: "a1b2c3d4-g0h2-f3j4k5l6m7n7-e5f5-7890" (8-4-16-4-4)
    // Format 2: "z9y8x7w6-v2u3-3210-r7q6p5o4n3n2-t9s8" (8-4-4-12-4)
    private val uuidRegex = """["']([a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4,}-[a-z0-9]{4,}-[a-z0-9]{4,})["']""".toRegex()

    // Numeric keys before "manga_info" and "chapter_read" in 'c' arrays
    private val mangaInfoRegex = """(0x[0-9a-fA-F]+|\d{10,18}),\s*["']manga_info["']""".toRegex()
    private val chapterReadRegex = """(0x[0-9a-fA-F]+|\d{10,18}),\s*["']chapter_read["']""".toRegex()

    fun extract(securityScript: String, normalizeScript: String): SecurityKeys {
        val mangaInfoKey = parseNumber(mangaInfoRegex.find(securityScript)?.groupValues?.get(1))
        val chapterReadKey = parseNumber(chapterReadRegex.find(securityScript)?.groupValues?.get(1))

        val allUuids = uuidRegex.findAll(securityScript)
            .map { it.groupValues[1] }
            .filter { it.contains("-") && it.length >= 30 }
            .distinct()
            .toList()

        return SecurityKeys(
            mangaInfo = mangaInfoKey,
            chapterRead = chapterReadKey,
            xVerificationKey1 = allUuids.getOrElse(0) { "" },
            xVerificationKey2 = allUuids.getOrElse(1) { "" },
            clientSignature = signatureRegex.find(normalizeScript)?.groupValues?.last() ?: "",
        )
    }

    private fun parseNumber(str: String?): Long {
        if (str == null) return 0L
        return if (str.startsWith("0x", ignoreCase = true)) {
            str.substring(2).toLongOrNull(16) ?: 0L
        } else {
            str.toLongOrNull() ?: 0L
        }
    }
}
