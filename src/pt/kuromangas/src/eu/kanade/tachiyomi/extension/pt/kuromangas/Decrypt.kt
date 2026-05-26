package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.util.Base64
import keiyoushi.utils.parseAs
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal class ApiDecryptInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!request.url.encodedPath.startsWith("/api/")) {
            return response
        }

        return response.use {
            val contentType = it.body.contentType()
            val bodyString = it.body.string()
            val decryptedBody = Decrypt.decodeResponseBody(
                body = bodyString,
                hostname = request.url.host,
            ) ?: bodyString

            it.newBuilder()
                .body(decryptedBody.toResponseBody(contentType))
                .build()
        }
    }
}

internal object Decrypt {
    private const val API_ENCRYPTION_KEY = "5ato8l674shksfE2oMwajkun9TuYTusF4jKdqEwhUEft9787147pasde345h"
    private const val HOST_MARKER = "x9_4v2_b"
    private const val API_VERSION_SUFFIX = "::v2"
    private const val OPEN_SSL_PREFIX = "Salted__"
    private const val KEY_BYTES = 16
    private const val IV_BYTES = 8

    private val utcDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun decodeResponseBody(body: String, hostname: String): String? {
        val payload = runCatching { body.parseAs<JsonObject>() }.getOrNull()
            ?: return null

        val encryptedPayload = payload["_v_secure"]?.jsonPrimitive?.contentOrNull
            ?: return null

        return candidateDates().firstNotNullOfOrNull { date ->
            runCatching {
                val decrypted = decryptPayload(encryptedPayload, keyFor(date, hostname))
                    .toString(Charsets.UTF_8)
                    .parseAs<JsonObject>()

                normalizePayload(decrypted).toString()
            }.getOrNull()
        } ?: throw IOException("Falha ao decodificar payload protegido da KuroMangas")
    }

    private fun normalizePayload(payload: JsonObject): JsonObject {
        val withoutSecure = JsonObject(payload.filterKeys { it != "_v_secure" })

        if (withoutSecure.hasKnownPayload()) {
            return withoutSecure
        }

        return withoutSecure.values
            .asSequence()
            .mapNotNull { it as? JsonObject }
            .firstOrNull { it.hasKnownPayload() }
            ?: withoutSecure.singleDynamicObject()
            ?: withoutSecure
    }

    private fun JsonObject.hasKnownPayload(): Boolean = this["token"] is JsonPrimitive ||
        (this["data"] is JsonArray && this["pagination"] is JsonObject) ||
        (this["manga"] is JsonObject && this["chapters"] is JsonArray) ||
        this["pages"] is JsonArray

    private fun JsonObject.singleDynamicObject(): JsonObject? {
        if (size != 1) return null

        val key = keys.first()
        if (!DYNAMIC_KEY_REGEX.matches(key)) return null

        return this[key] as? JsonObject
    }

    private fun decryptPayload(encryptedPayload: String, passphrase: String): ByteArray {
        val raw = Base64.decode(encryptedPayload, Base64.DEFAULT)
        val prefix = OPEN_SSL_PREFIX.toByteArray(Charsets.UTF_8)

        if (raw.size <= prefix.size + 8 || !raw.copyOfRange(0, prefix.size).contentEquals(prefix)) {
            throw IOException("Payload protegido nao usa o formato OpenSSL esperado")
        }

        val salt = raw.copyOfRange(prefix.size, prefix.size + 8)
        val ciphertext = raw.copyOfRange(prefix.size + 8, raw.size)
        val keyAndIv = openSslKeyAndIv(passphrase, salt)

        return rabbitCrypt(
            data = ciphertext,
            keyBytes = keyAndIv.first,
            ivBytes = keyAndIv.second,
        )
    }

    private fun keyFor(date: String, hostname: String): String {
        val dynamic = "$date$hostname$API_VERSION_SUFFIX$HOST_MARKER"
            .md5()
            .take(8)

        return API_ENCRYPTION_KEY + dynamic
    }

    private fun candidateDates(): List<String> {
        val now = Date()
        return synchronized(utcDateFormat) {
            val today = utcDateFormat.format(now)
            val yesterday = utcDateFormat.format(Date(now.time - DAY_MILLIS))
            val tomorrow = utcDateFormat.format(Date(now.time + DAY_MILLIS))
            listOf(today, yesterday, tomorrow).distinct()
        }
    }

    private fun openSslKeyAndIv(passphrase: String, salt: ByteArray): Pair<ByteArray, ByteArray> {
        val password = passphrase.toByteArray(Charsets.UTF_8)
        val targetSize = KEY_BYTES + IV_BYTES
        var derived = byteArrayOf()
        var block = byteArrayOf()

        while (derived.size < targetSize) {
            block = MessageDigest.getInstance("MD5").apply {
                if (block.isNotEmpty()) update(block)
                update(password)
                update(salt)
            }.digest()
            derived += block
        }

        return derived.copyOfRange(0, KEY_BYTES) to derived.copyOfRange(KEY_BYTES, targetSize)
    }

    private fun rabbitCrypt(data: ByteArray, keyBytes: ByteArray, ivBytes: ByteArray): ByteArray {
        val key = keyBytes.toWords().map { it.swapEndian() }
        val x = intArrayOf(
            key[0],
            key[3] shl 16 or (key[2] ushr 16),
            key[1],
            key[0] shl 16 or (key[3] ushr 16),
            key[2],
            key[1] shl 16 or (key[0] ushr 16),
            key[3],
            key[2] shl 16 or (key[1] ushr 16),
        )
        val c = intArrayOf(
            key[2] shl 16 or (key[2] ushr 16),
            key[0] and -0x10000 or (key[1] and 0x0000ffff),
            key[3] shl 16 or (key[3] ushr 16),
            key[1] and -0x10000 or (key[2] and 0x0000ffff),
            key[0] shl 16 or (key[0] ushr 16),
            key[2] and -0x10000 or (key[3] and 0x0000ffff),
            key[1] shl 16 or (key[1] ushr 16),
            key[3] and -0x10000 or (key[0] and 0x0000ffff),
        )
        val state = RabbitState(x, c)

        repeat(4) { state.next() }
        for (index in 0 until 8) {
            c[index] = c[index] xor x[(index + 4) and 7]
        }

        ivBytes.mixInto(state)

        val output = ByteArray(data.size)
        var offset = 0
        while (offset < data.size) {
            val stream = state.keyStream()
            val length = minOf(stream.size, data.size - offset)

            for (index in 0 until length) {
                output[offset + index] = (data[offset + index].toInt() xor stream[index].toInt()).toByte()
            }

            offset += length
        }

        return output
    }

    private fun ByteArray.mixInto(state: RabbitState) {
        if (isEmpty()) return

        val iv = toWords()
        val iv0 = iv[0].swapEndian()
        val iv1 = iv[1].swapEndian()
        val iv2 = iv0 ushr 16 or (iv1 and -0x10000)
        val iv3 = iv1 shl 16 or (iv0 and 0x0000ffff)
        val c = state.c

        c[0] = c[0] xor iv0
        c[1] = c[1] xor iv2
        c[2] = c[2] xor iv1
        c[3] = c[3] xor iv3
        c[4] = c[4] xor iv0
        c[5] = c[5] xor iv2
        c[6] = c[6] xor iv1
        c[7] = c[7] xor iv3

        repeat(4) { state.next() }
    }

    private fun ByteArray.toWords(): IntArray {
        val words = IntArray(size / 4)

        for (index in words.indices) {
            val offset = index * 4
            words[index] =
                ((this[offset].toInt() and 0xff) shl 24) or
                ((this[offset + 1].toInt() and 0xff) shl 16) or
                ((this[offset + 2].toInt() and 0xff) shl 8) or
                (this[offset + 3].toInt() and 0xff)
        }

        return words
    }

    private fun RabbitState.keyStream(): ByteArray {
        next()

        return intArrayOf(
            x[0] xor (x[5] ushr 16) xor (x[3] shl 16),
            x[2] xor (x[7] ushr 16) xor (x[5] shl 16),
            x[4] xor (x[1] ushr 16) xor (x[7] shl 16),
            x[6] xor (x[3] ushr 16) xor (x[1] shl 16),
        )
            .map { it.swapEndian() }
            .flatMap { it.toBytes() }
            .toByteArray()
    }

    private fun RabbitState.next() {
        val oldC = c.copyOf()

        c[0] += 0x4d34d34d + carry
        c[1] += 0xd34d34d3.toInt() + c[0].carryFrom(oldC[0])
        c[2] += 0x34d34d34 + c[1].carryFrom(oldC[1])
        c[3] += 0x4d34d34d + c[2].carryFrom(oldC[2])
        c[4] += 0xd34d34d3.toInt() + c[3].carryFrom(oldC[3])
        c[5] += 0x34d34d34 + c[4].carryFrom(oldC[4])
        c[6] += 0x4d34d34d + c[5].carryFrom(oldC[5])
        c[7] += 0xd34d34d3.toInt() + c[6].carryFrom(oldC[6])
        carry = c[7].carryFrom(oldC[7])

        val g = IntArray(8)
        for (index in 0 until 8) {
            val gx = x[index] + c[index]
            val low = gx and 0x0000ffff
            val high = gx ushr 16
            val highSquare = ((((low.toLong() * low) ushr 17) + low.toLong() * high) ushr 15) + high.toLong() * high
            val lowSquare = (gx and -0x10000) * gx + (gx and 0x0000ffff) * gx

            g[index] = highSquare.toInt() xor lowSquare
        }

        x[0] = g[0] + (g[7] shl 16 or (g[7] ushr 16)) + (g[6] shl 16 or (g[6] ushr 16))
        x[1] = g[1] + (g[0] shl 8 or (g[0] ushr 24)) + g[7]
        x[2] = g[2] + (g[1] shl 16 or (g[1] ushr 16)) + (g[0] shl 16 or (g[0] ushr 16))
        x[3] = g[3] + (g[2] shl 8 or (g[2] ushr 24)) + g[1]
        x[4] = g[4] + (g[3] shl 16 or (g[3] ushr 16)) + (g[2] shl 16 or (g[2] ushr 16))
        x[5] = g[5] + (g[4] shl 8 or (g[4] ushr 24)) + g[3]
        x[6] = g[6] + (g[5] shl 16 or (g[5] ushr 16)) + (g[4] shl 16 or (g[4] ushr 16))
        x[7] = g[7] + (g[6] shl 8 or (g[6] ushr 24)) + g[5]
    }

    private fun Int.carryFrom(previous: Int): Int = if (Integer.compareUnsigned(this, previous) < 0) 1 else 0

    private fun Int.swapEndian(): Int = (((this shl 8) or (this ushr 24)) and 0x00ff00ff) or
        (((this shl 24) or (this ushr 8)) and -0x00ff0100)

    private fun Int.toBytes(): List<Byte> = listOf(
        (this ushr 24).toByte(),
        (this ushr 16).toByte(),
        (this ushr 8).toByte(),
        toByte(),
    )

    private fun String.md5(): String {
        val hash = MessageDigest.getInstance("MD5").digest(toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    private class RabbitState(
        val x: IntArray,
        val c: IntArray,
        var carry: Int = 0,
    )

    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    private val DYNAMIC_KEY_REGEX = Regex("""_[A-Za-z0-9]+""")
}
