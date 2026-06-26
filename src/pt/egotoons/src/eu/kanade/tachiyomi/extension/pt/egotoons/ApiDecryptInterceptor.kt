package eu.kanade.tachiyomi.extension.pt.egotoons

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class ApiDecryptInterceptor : Interceptor {

    private val derivedKey: ByteArray by lazy { deriveKey() }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful || !request.url.encodedPath.contains("/api/obras")) {
            return response
        }

        val bodyString = response.body.string()

        // Handle empty or unencrypted responses gracefully
        if (!bodyString.contains(":")) {
            return response.newBuilder()
                .body(bodyString.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val parts = bodyString.split(":")
        if (parts.size != 2) {
            return response.newBuilder()
                .body(bodyString.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val ivStr = parts[0]
        val ciphertextStr = parts[1]

        val decryptedData = runCatching { decrypt(ivStr, ciphertextStr) }
            .getOrElse { return response.newBuilder().body(bodyString.toResponseBody("application/json".toMediaType())).build() }

        return response.newBuilder()
            .body(decryptedData.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun decrypt(ivHex: String, ciphertextHex: String): String {
        val iv = ivHex.decodeHex()
        val ciphertext = ciphertextHex.decodeHex()

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(derivedKey, "AES")
        val ivParameterSpec = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun deriveKey(): ByteArray {
        val hashStr = "fqegqweg9u23wi4go32wh4gij".repeat(4) + "salt"
        return MessageDigest.getInstance("SHA-256").digest(hashStr.toByteArray(Charsets.UTF_8))
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
