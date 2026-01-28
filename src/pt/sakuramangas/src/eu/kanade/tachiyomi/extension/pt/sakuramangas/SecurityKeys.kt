package eu.kanade.tachiyomi.extension.pt.sakuramangas

data class SecurityKeys(
    val mangaInfo: Long,
    val chapterRead: Long,
    val xVerificationKey1: String,
    val xVerificationKey2: String,
    val clientSignature: String,
    val imageXRequestedWith: String,
    val imageXSignatureVersion: String,
)

object SecurityKeysExtractor {

    private val signatureRegex = """['"](\w{5}-\w{5}-\w{6})['"]""".toRegex()
    private val verificationKey1Regex = """['"]([a-z0-9-]{30,50})['"],\s*['"]X-Verification-Key-1['"]""".toRegex(RegexOption.IGNORE_CASE)
    private val verificationKey2Regex = """['"]([a-z0-9-]{30,50})['"],\s*['"]X-Verification-Key-2['"]""".toRegex(RegexOption.IGNORE_CASE)
    private val mangaInfoRegex = """(0x[0-9a-fA-F]+|\d{10,18}),\s*["']manga_info["']""".toRegex()
    private val chapterReadRegex = """(0x[0-9a-fA-F]+|\d{10,18}),\s*["']chapter_read["']""".toRegex()
    private val clientSignatureRegex = """['"](\w{5}-\w{5}-\w{6})['"],\s*['"]client_signature['"]""".toRegex(RegexOption.IGNORE_CASE)
    private val imageXRequestedWithRegex = """['"]X-Requested-With['"],\s*['"]([a-z0-9]{10,30})['"],\s*['"]append['"]""".toRegex(RegexOption.IGNORE_CASE)
    private val imageXSignatureVersionRegex = """['"]X-Signature-Version['"],\s*['"]([a-z0-9-]+)['"]""".toRegex(RegexOption.IGNORE_CASE)

    fun extract(securityScript: String, normalizeScript: String): SecurityKeys {
        val mangaInfoKey = parseNumber(mangaInfoRegex.find(securityScript)?.groupValues?.get(1))
        val chapterReadKey = parseNumber(chapterReadRegex.find(securityScript)?.groupValues?.get(1))

        val key1 = verificationKey1Regex.find(securityScript)?.groupValues?.get(1).orEmpty()
        val key2 = verificationKey2Regex.find(securityScript)?.groupValues?.get(1).orEmpty()

        val signature = clientSignatureRegex.find(securityScript)?.groupValues?.get(1)
            ?: signatureRegex.find(securityScript)?.groupValues?.last()
            ?: signatureRegex.find(normalizeScript)?.groupValues?.last()
                .orEmpty()

        return SecurityKeys(
            mangaInfo = mangaInfoKey,
            chapterRead = chapterReadKey,
            xVerificationKey1 = key1,
            xVerificationKey2 = key2,
            clientSignature = signature,
            imageXRequestedWith = "",
            imageXSignatureVersion = "",
        )
    }

    fun extractImageHeaders(chapterScript: String, baseKeys: SecurityKeys) = baseKeys.copy(
        imageXRequestedWith = imageXRequestedWithRegex.find(chapterScript)?.groupValues?.get(1).orEmpty(),
        imageXSignatureVersion = imageXSignatureVersionRegex.find(chapterScript)?.groupValues?.get(1).orEmpty(),
    )

    private fun parseNumber(str: String?): Long {
        if (str == null) return 0L
        return if (str.startsWith("0x", ignoreCase = true)) {
            str.substring(2).toLongOrNull(16) ?: 0L
        } else {
            str.toLongOrNull() ?: 0L
        }
    }
}
