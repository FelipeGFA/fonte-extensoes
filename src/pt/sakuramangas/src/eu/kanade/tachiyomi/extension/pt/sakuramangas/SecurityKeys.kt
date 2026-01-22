package eu.kanade.tachiyomi.extension.pt.sakuramangas

data class SecurityKeys(
    val xVerificationKey1: String,
    val xVerificationKey2: String,
    val clientSignature: String,
)

object SecurityKeysExtractor {

    private val signatureRegex = """['"](\w{5}-\w{5}-\w{6})['"]""".toRegex()
    private val uuidRegex = """["']([a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4,}-[a-z0-9]{4,}-[a-z0-9]{4,})["']""".toRegex()

    fun extract(securityScript: String, normalizeScript: String): SecurityKeys {
        val allUuids = uuidRegex.findAll(securityScript)
            .map { it.groupValues[1] }
            .filter { it.contains("-") && it.length >= 30 }
            .distinct()
            .toList()

        return SecurityKeys(
            xVerificationKey1 = allUuids.getOrElse(0) { "" },
            xVerificationKey2 = allUuids.getOrElse(1) { "" },
            clientSignature = signatureRegex.find(normalizeScript)?.groupValues?.last() ?: "",
        )
    }
}
