package eu.kanade.tachiyomi.extension.pt.sakuramangas

import android.util.Base64
import keiyoushi.utils.parseAs
import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sin

object YggdrasilCipher {

    object CryptoUtils {

        private fun bufferToHex(bytes: ByteArray): String {
            return bytes.joinToString(separator = "") { byte ->
                "%02x".format(byte.toInt() and 0xFF)
            }
        }

        fun hexToBytes(hex: String): ByteArray {
            require(hex.length % 2 == 0) { "Hex string must have an even length." }

            val result = ByteArray(hex.length / 2)
            for (i in hex.indices step 2) {
                val byte = hex.substring(i, i + 2).toInt(16)
                result[i / 2] = byte.toByte()
            }
            return result
        }

        private fun digest(algorithm: String, input: String): String {
            val md = MessageDigest.getInstance(algorithm)
            val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
            return bufferToHex(bytes)
        }

        fun sha256(input: String): String {
            return digest("SHA-256", input)
        }

        fun sha512(input: String): String {
            return digest("SHA-512", input)
        }

        fun md5(input: String): String {
            return digest("MD5", input)
        }
    }

    /**
     * Decrypts using FREYA cipher algorithm.
     * Equivalent to JavaScript's FREYA implementation:
     * 1. Computes MD5 of subtoken and parses first 8 hex chars as 32-bit integer
     * 2. For each encrypted byte, computes a pseudo-random byte based on sin/log
     * 3. XORs encrypted byte with the pseudo-random byte and converts to chars
     */
    private fun decryptFreya(encrypted: ByteArray, subtoken: String): String {
        // Step 1: MD5(subtoken) and parse first 8 hex chars as number (up to 0xFFFFFFFF)
        // In JS this is a 32-bit unsigned value used as a Number (double), so we keep it as Double here
        val md5Hex = CryptoUtils.md5(subtoken)
        val seed = md5Hex.substring(0, 8).toLong(16).toDouble()

        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            val byteValue = encrypted[i].toInt() and 0xFF

            // Equivalent to JS:
            // floor(abs(sin(seed + i) * log(seed + 1)) * 256) % 256
            val t = seed + i.toDouble()
            val rand = floor(abs(sin(t) * ln(seed + 1.0)) * 256.0)
                .toInt() and 0xFF

            val decryptedByte = byteValue xor rand
            result[i] = decryptedByte.toChar()
        }

        return String(result)
    }

    /**
     * Decrypts using FENRIR cipher algorithm.
     * Equivalent to JavaScript's FENRIR implementation:
     * 1. Generates SHA512 hash of subtoken and converts to bytes
     * 2. Initializes previous byte with first byte of key
     * 3. For each encrypted byte:
     *    - XORs encrypted byte with key byte and previous byte
     *    - Updates previous byte to the original encrypted byte (before XOR)
     * 4. Converts resulting bytes to string (bytes -> chars)
     */
    private fun decryptFenrir(encrypted: ByteArray, subtoken: String): String {
        // Step 1: Generate SHA512 hash and convert to bytes
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha512Hex)
        val keyLength = keyBytes.size

        // Step 2: Initialize previous byte with first byte of key
        var prevByte = keyBytes[0].toInt() and 0xFF

        // Step 3: Decrypt each byte
        val result = CharArray(encrypted.size)
        for (i in encrypted.indices) {
            val keyByte = keyBytes[i % keyLength].toInt() and 0xFF
            val encryptedByte = encrypted[i].toInt() and 0xFF

            // XOR: encryptedByte ^ keyByte ^ prevByte
            val decryptedByte = (encryptedByte xor keyByte xor prevByte) and 0xFF
            result[i] = decryptedByte.toChar()

            // Update previous byte to the original encrypted byte (before XOR)
            prevByte = encryptedByte
        }

        return String(result)
    }

    /**
     * Decrypts using HEIMDALL cipher algorithm.
     * Equivalent to JavaScript's HEIMDALL implementation:
     * 1. Generates SHA256 hash of subtoken and converts to bytes
     * 2. For each encrypted byte:
     *    - Subtracts keyBytes[(i + 1) % keyLength] and adjusts modulo 256
     *    - Applies bit permutation (specific bit mapping)
     *    - XORs with keyBytes[i % keyLength]
     * 3. Converts resulting bytes to string (bytes -> chars)
     */
    private fun decryptHeimdall(encrypted: ByteArray, subtoken: String): String {
        // Step 1: Generate SHA256 hash and convert to bytes
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha256Hex)
        val keyLength = keyBytes.size

        // Step 2: Bit permutation function
        // Maps bits: 0->7, 1->5, 2->3, 3->1, 4->6, 5->2, 6->4, 7->0
        fun permuteBits(value: Int): Int {
            var result = 0
            if (value and (1 shl 0) != 0) result = result or (1 shl 7)
            if (value and (1 shl 1) != 0) result = result or (1 shl 5)
            if (value and (1 shl 2) != 0) result = result or (1 shl 3)
            if (value and (1 shl 3) != 0) result = result or (1 shl 1)
            if (value and (1 shl 4) != 0) result = result or (1 shl 6)
            if (value and (1 shl 5) != 0) result = result or (1 shl 2)
            if (value and (1 shl 6) != 0) result = result or (1 shl 4)
            if (value and (1 shl 7) != 0) result = result or (1 shl 0)
            return result
        }

        // Step 3: Decrypt each byte
        val result = CharArray(encrypted.size)
        for (i in encrypted.indices) {
            var byteValue = encrypted[i].toInt() and 0xFF

            // Subtract keyBytes[(i + 1) % keyLength] and adjust modulo 256
            val subtractKey = keyBytes[(i + 1) % keyLength].toInt() and 0xFF
            byteValue = (byteValue - subtractKey + 256) % 256

            // Apply bit permutation
            byteValue = permuteBits(byteValue)

            // XOR with keyBytes[i % keyLength]
            val xorKey = keyBytes[i % keyLength].toInt() and 0xFF
            byteValue = byteValue xor xorKey

            result[i] = byteValue.toChar()
        }

        return String(result)
    }

    /**
     * Decrypts using JORMUNGANDR cipher algorithm.
     * Equivalent to JavaScript's JORMUNGANDR implementation:
     * 1. Generates SHA256 hash of subtoken and converts to bytes
     * 2. For each encrypted byte, applies circular bit rotation
     * 3. Rotation amount is determined by keyBytes[i % keyLength] % 8
     * 4. Converts resulting bytes to string (bytes -> chars)
     */
    private fun decryptJormungandr(encrypted: ByteArray, subtoken: String): String {
        // Step 1: Generate SHA256 hash and convert to bytes
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha256Hex)
        val keyLength = keyBytes.size

        // Step 2: Circular bit rotation function
        // Equivalent to: ((value >> shift) | (value << (8 - shift))) & 255
        fun circularRotate(value: Int, shift: Int): Int {
            val shiftAmount = shift and 7 // shift &= 7
            if (shiftAmount == 0) {
                return value
            }
            return ((value shr shiftAmount) or (value shl (8 - shiftAmount))) and 0xFF
        }

        // Step 3: Apply rotation to each byte
        val result = ByteArray(encrypted.size)
        for (i in encrypted.indices) {
            // Calculate rotation amount: keyBytes[i % keyLength] % 8
            val rotationAmount = (keyBytes[i % keyLength].toInt() and 0xFF) % 8
            // Apply circular rotation
            result[i] = circularRotate(encrypted[i].toInt() and 0xFF, rotationAmount).toByte()
        }

        // Step 4: Convert bytes to string (equivalent to _bytesToString)
        // Each byte (0-255) becomes a char
        return String(result.map { (it.toInt() and 0xFF).toChar() }.toCharArray())
    }

    /**
     * Decrypts using LOKI cipher algorithm.
     * Equivalent to JavaScript's LOKI implementation:
     * 1. Generates SHA256 hash of subtoken and gets first byte
     * 2. Calculates chunk size: (firstByte % 8) + 3
     * 3. Converts bytes to string (each byte becomes a char)
     * 4. Splits string into chunks of calculated size
     * 5. For each chunk: if index is even, reverses it; if odd, keeps it
     * 6. XORs each character with subtoken (using modulo to repeat)
     */
    private fun decryptLoki(encrypted: ByteArray, subtoken: String): String {
        // Step 1: Generate SHA256 hash and get first byte
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha256Hex)
        val firstByte = keyBytes[0].toInt() and 0xFF

        // Step 2: Calculate chunk size: (firstByte % 8) + 3
        val chunkSize = (firstByte % 8) + 3

        // Step 3: Convert bytes to string (equivalent to _bytesToString)
        // Each byte (0-255) becomes a char
        val inputString = String(encrypted.map { (it.toInt() and 0xFF).toChar() }.toCharArray())

        // Step 4: Split string into chunks
        val chunks = mutableListOf<String>()
        var i = 0
        while (i < inputString.length) {
            val end = minOf(i + chunkSize, inputString.length)
            chunks.add(inputString.substring(i, end))
            i += chunkSize
        }

        // Step 5: Process chunks - reverse even-indexed chunks, keep odd-indexed ones
        val processedString = StringBuilder()
        chunks.forEachIndexed { index, chunk ->
            if (index % 2 == 0) {
                // Reverse even-indexed chunks
                processedString.append(chunk.reversed())
            } else {
                // Keep odd-indexed chunks as-is
                processedString.append(chunk)
            }
        }

        // Step 6: XOR each character with subtoken
        val result = CharArray(processedString.length)
        for (j in processedString.indices) {
            val charCode = processedString[j].code
            val keyCharCode = subtoken[j % subtoken.length].code
            val xorResult = charCode xor keyCharCode
            result[j] = xorResult.toChar()
        }

        return String(result)
    }

    private fun decryptNidhogg(encrypted: ByteArray, subtoken: String): String {
        // sha512(subtoken) -> hex -> bytes (64 bytes of key material)
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha512Hex)

        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            var b = encrypted[i].toInt() and 0xFF

            val k1 = keyBytes[i % 64].toInt() and 0xFF
            val k2 = keyBytes[(i + 16) % 64].toInt() and 0xFF

            b = b xor ((b shr 4) and 0x0F)
            b = (b - k1 + 256) and 0xFF
            b = b.inv() and 0xFF
            b = b xor k2
            b = ((b shr 3) or (b shl 5)) and 0xFF
            b = b xor k1

            result[i] = b.toChar()
        }

        return String(result)
    }

    /**
     * Decrypts using ODIN cipher algorithm.
     * Equivalent to JavaScript's ODIN implementation:
     * 1. Generates SHA256 hash of subtoken and converts to bytes (for S-box permutation)
     * 2. Creates S-box array [0, 1, 2, ..., 255] and permutes it using RC4-like algorithm
     * 3. Builds inverse S-box array
     * 4. Generates SHA512 hash of subtoken and converts to bytes (for XOR)
     * 5. For each encrypted byte: XOR with key, apply inverse S-box, convert to char
     */
    private fun decryptOdin(encrypted: ByteArray, subtoken: String): String {
        // Step 1: Generate SHA256 hash and convert to bytes (for S-box permutation)
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes1 = CryptoUtils.hexToBytes(sha256Hex)

        // Step 2: Create S-box [0, 1, 2, ..., 255] and permute it
        val sBox = IntArray(256) { it }
        var j = 0

        for (i in 0 until 256) {
            // j = (j + S[i] + keyBytes1[i % keyLength]) % 256
            j = (j + sBox[i] + (keyBytes1[i % keyBytes1.size].toInt() and 0xFF)) % 256
            // Swap S[i] with S[j]
            val temp = sBox[i]
            sBox[i] = sBox[j]
            sBox[j] = temp
        }

        // Step 3: Build inverse S-box array
        // inverseSbox[sBox[i]] = i
        val inverseSbox = IntArray(256)
        for (i in 0 until 256) {
            inverseSbox[sBox[i]] = i
        }

        // Step 4: Generate SHA512 hash and convert to bytes (for XOR)
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes2 = CryptoUtils.hexToBytes(sha512Hex)
        val keyLength2 = keyBytes2.size

        // Step 5: Decrypt each byte
        val result = CharArray(encrypted.size)
        for (i in encrypted.indices) {
            // XOR encrypted byte with key byte
            val xorResult =
                (encrypted[i].toInt() and 0xFF) xor (keyBytes2[i % keyLength2].toInt() and 0xFF)
            // Apply inverse S-box
            val decryptedByte = inverseSbox[xorResult]
            // Convert to char
            result[i] = decryptedByte.toChar()
        }

        return String(result)
    }

    /**
     * Decrypts using SLEIPNIR cipher algorithm.
     * Equivalent to JavaScript's SLEIPNIR implementation:
     * 1. Converts bytes to string (each byte becomes a char)
     * 2. Generates SHA512 hash of subtoken and converts to bytes
     * 3. Creates permutation array by reverse shuffling indices
     * 4. Builds inverse permutation array
     * 5. Reorders characters using inverse permutation
     */
    private fun decryptSleipnir(encrypted: ByteArray, subtoken: String): String {
        // Convert bytes to string (equivalent to _bytesToString)
        // Each byte (0-255) becomes a char
        val inputString = String(encrypted.map { (it.toInt() and 0xFF).toChar() }.toCharArray())
        val length = inputString.length

        // Generate SHA512 hash and convert to bytes
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha512Hex)
        val keyLength = keyBytes.size

        // Create array of indices [0, 1, 2, ..., length-1]
        val indices = IntArray(length) { it }

        // Reverse shuffle: from length-1 down to 1
        for (i in length - 1 downTo 1) {
            // Calculate swap index: keyBytes[i % keyLength] % (i + 1)
            val swapIndex = (keyBytes[i % keyLength].toInt() and 0xFF) % (i + 1)
            // Swap indices[i] with indices[swapIndex]
            val temp = indices[i]
            indices[i] = indices[swapIndex]
            indices[swapIndex] = temp
        }

        // Build inverse permutation array
        // inversePermutation[indices[i]] = i
        val inversePermutation = IntArray(length)
        for (i in 0 until length) {
            inversePermutation[indices[i]] = i
        }

        // Reorder characters using inverse permutation
        val result = CharArray(length)
        for (i in 0 until length) {
            result[inversePermutation[i]] = inputString[i]
        }

        return String(result)
    }

    /**
     * Decrypts using THOR cipher algorithm.
     * Equivalent to JavaScript's THOR implementation:
     * 1. Generates SHA256 hash of subtoken and converts to bytes
     * 2. Interprets hash bytes as little-endian 32-bit words
     * 3. Interprets encrypted bytes as little-endian 32-bit words and XORs with key words
     * 4. Converts resulting bytes to string (bytes -> chars)
     * 5. Removes PKCS-style padding (up to 4 bytes) if present
     */
    private fun decryptThor(encrypted: ByteArray, subtoken: String): String {
        // Step 1: Generate SHA256 hash and convert to bytes
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha256Hex)

        // Step 2: Interpret hash bytes as little-endian 32-bit words
        val keyWordCount = keyBytes.size / 4
        val keyWords = IntArray(keyWordCount)
        for (i in 0 until keyWordCount) {
            val base = i * 4
            val b0 = keyBytes[base].toInt() and 0xFF
            val b1 = keyBytes[base + 1].toInt() and 0xFF
            val b2 = keyBytes[base + 2].toInt() and 0xFF
            val b3 = keyBytes[base + 3].toInt() and 0xFF
            keyWords[i] = b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
        }
        val keyLen = keyWords.size

        // Step 3: Interpret encrypted bytes as little-endian 32-bit words and XOR with key words
        val outBytes = encrypted.copyOf()
        var blockIndex = 0
        var i = 0
        while (i + 4 <= encrypted.size) {
            val b0 = encrypted[i].toInt() and 0xFF
            val b1 = encrypted[i + 1].toInt() and 0xFF
            val b2 = encrypted[i + 2].toInt() and 0xFF
            val b3 = encrypted[i + 3].toInt() and 0xFF
            val word = b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)

            val keyWord = keyWords[blockIndex % keyLen]
            val xored = word xor keyWord

            outBytes[i] = (xored and 0xFF).toByte()
            outBytes[i + 1] = ((xored ushr 8) and 0xFF).toByte()
            outBytes[i + 2] = ((xored ushr 16) and 0xFF).toByte()
            outBytes[i + 3] = ((xored ushr 24) and 0xFF).toByte()

            blockIndex++
            i += 4
        }

        // Step 4: Convert resulting bytes to string (bytes -> chars, 0-255)
        var result = String(outBytes.map { (it.toInt() and 0xFF).toChar() }.toCharArray())

        // Step 5: Remove PKCS-style padding (up to 4 bytes)
        if (result.isNotEmpty()) {
            val padLen = result.last().code
            if (padLen in 1..4 && result.length >= padLen) {
                var validPadding = true
                for (p in 1..padLen) {
                    if (result[result.length - p].code != padLen) {
                        validPadding = false
                        break
                    }
                }
                if (validPadding) {
                    result = result.dropLast(padLen)
                }
            }
        }

        return result
    }

    /**
     * Decrypts using BIFROST cipher algorithm.
     * 1. Generates SHA256 hash of subtoken and converts to bytes
     * 2. For each pair of bytes (i, i+1):
     *    - XOR bytes[i] with key[(i+1) % keyLength]
     *    - XOR bytes[i+1] with key[i % keyLength]
     */
    private fun decryptBifrost(encrypted: ByteArray, subtoken: String): String {
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha256Hex)
        val keyLength = keyBytes.size

        val result = CharArray(encrypted.size)
        var i = 0
        while (i < encrypted.size) {
            val byte1 = encrypted[i].toInt() and 0xFF
            val key1 = keyBytes[(i + 1) % keyLength].toInt() and 0xFF
            result[i] = (byte1 xor key1).toChar()

            if (i + 1 < encrypted.size) {
                val byte2 = encrypted[i + 1].toInt() and 0xFF
                val key2 = keyBytes[i % keyLength].toInt() and 0xFF
                result[i + 1] = (byte2 xor key2).toChar()
            }
            i += 2
        }
        return String(result)
    }

    /**
     * Decrypts using FAFNIR cipher algorithm.
     * 1. Generates SHA512 hash of subtoken and converts to bytes
     * 2. Maintains accumulator, adds key byte to it each iteration
     * 3. Subtracts accumulator from each encrypted byte
     */
    private fun decryptFafnir(encrypted: ByteArray, subtoken: String): String {
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha512Hex)
        val keyLength = keyBytes.size

        var accumulator = 0
        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            accumulator += keyBytes[i % keyLength].toInt() and 0xFF
            var byteValue = (encrypted[i].toInt() and 0xFF) - accumulator
            while (byteValue < 0) {
                byteValue += 256
            }
            result[i] = (byteValue and 0xFF).toChar()
        }
        return String(result)
    }

    /**
     * Decrypts using GJALLARHORN cipher algorithm.
     * 1. Generates SHA512 hash of subtoken and converts to bytes
     * 2. XOR byte with key byte and previous accumulator
     * 3. Accumulator is updated: accumulator += original byte
     */
    private fun decryptGjallarhorn(encrypted: ByteArray, subtoken: String): String {
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha512Hex)
        val keyLength = keyBytes.size

        var accumulator = 0
        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            val byteValue = encrypted[i].toInt() and 0xFF
            val keyValue = keyBytes[i % keyLength].toInt() and 0xFF
            val decrypted = byteValue xor keyValue xor accumulator
            accumulator = (accumulator + byteValue) and 0xFF
            result[i] = (decrypted and 0xFF).toChar()
        }
        return String(result)
    }

    /**
     * Decrypts using GUNGNIR cipher algorithm.
     * 1. Generates SHA256 hash of subtoken and converts to bytes
     * 2. For each byte:
     *    - If key is even: swap nibbles (high 4 bits <-> low 4 bits)
     *    - If key is odd: XOR with 128
     * 3. XOR result with key byte
     */
    private fun decryptGungnir(encrypted: ByteArray, subtoken: String): String {
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha256Hex)
        val keyLength = keyBytes.size

        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            var byteValue = encrypted[i].toInt() and 0xFF
            val keyValue = keyBytes[i % keyLength].toInt() and 0xFF

            if (keyValue % 2 == 0) {
                // Swap nibbles: (low 4 bits) << 4 | (high 4 bits) >> 4
                byteValue = ((byteValue and 0x0F) shl 4) or ((byteValue and 0xF0) ushr 4)
            } else {
                // XOR with 128
                byteValue = byteValue xor 128
            }

            byteValue = byteValue xor keyValue
            result[i] = byteValue.toChar()
        }
        return String(result)
    }

    /**
     * Decrypts using RAGNAROK cipher algorithm.
     * 1. Generates SHA512 hash of subtoken and converts to bytes
     * 2. For each byte:
     *    - Rotate left 3 bits
     *    - If key < 128: bitwise NOT
     *    - If key is even: XOR with key
     *    - If key is odd: subtract key (with wrap)
     */
    private fun decryptRagnarok(encrypted: ByteArray, subtoken: String): String {
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha512Hex)
        val keyLength = keyBytes.size

        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            var byteValue = encrypted[i].toInt() and 0xFF
            val keyValue = keyBytes[i % keyLength].toInt() and 0xFF

            // Rotate left 3 bits
            byteValue = ((byteValue shl 3) or (byteValue ushr 5)) and 0xFF

            // If key < 128: bitwise NOT
            if (keyValue < 128) {
                byteValue = byteValue.inv() and 0xFF
            }

            // If key is even: XOR; if odd: subtract
            if (keyValue % 2 == 0) {
                byteValue = byteValue xor keyValue
            } else {
                byteValue = byteValue - keyValue
                while (byteValue < 0) {
                    byteValue += 256
                }
            }

            result[i] = byteValue.toChar()
        }
        return String(result)
    }

    /**
     * Decrypts using SKADI cipher algorithm.
     * 1. Generates SHA512 hash of subtoken and converts to bytes
     * 2. XOR byte with key byte
     * 3. Apply bit mask: (result & 0xF0) | (~result & 0x0F)
     */
    private fun decryptSkadi(encrypted: ByteArray, subtoken: String): String {
        val sha512Hex = CryptoUtils.sha512(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha512Hex)
        val keyLength = keyBytes.size

        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            val byteValue = encrypted[i].toInt() and 0xFF
            val keyValue = keyBytes[i % keyLength].toInt() and 0xFF
            val xorResult = byteValue xor keyValue
            // Apply bit mask: high nibble OR inverted low nibble
            val finalValue = (xorResult and 0xF0) or ((xorResult.inv()) and 0x0F)
            result[i] = (finalValue and 0xFF).toChar()
        }
        return String(result)
    }

    /**
     * Decrypts using YMIR cipher algorithm.
     * 1. Generates SHA256 hash of subtoken and converts to bytes
     * 2. Maintains accumulator
     * 3. For each byte: subtract accumulator, XOR with key, update accumulator
     */
    private fun decryptYmir(encrypted: ByteArray, subtoken: String): String {
        val sha256Hex = CryptoUtils.sha256(subtoken)
        val keyBytes = CryptoUtils.hexToBytes(sha256Hex)
        val keyLength = keyBytes.size

        var accumulator = 0
        val result = CharArray(encrypted.size)

        for (i in encrypted.indices) {
            val byteValue = encrypted[i].toInt() and 0xFF
            val keyValue = keyBytes[i % keyLength].toInt() and 0xFF

            // Subtract accumulator
            var temp = byteValue - accumulator
            while (temp < 0) {
                temp += 256
            }

            // XOR with key
            val decrypted = temp xor keyValue

            // Update accumulator: (original byte + key) & 0xFF
            accumulator = (byteValue + keyValue) and 0xFF

            result[i] = (decrypted and 0xFF).toChar()
        }
        return String(result)
    }

    fun decipher(
        encryptedEphemeralKey: SakuraMangaChapterReadEphemeralKeyDto,
        subtoken: String,
    ): String {
        val cipherName = encryptedEphemeralKey.cipher.uppercase()

        // Payload is Base64, same as Yggdrasil JS (atob)
        val payloadBytes = Base64.decode(encryptedEphemeralKey.payload, Base64.DEFAULT)

        return when (cipherName) {
            "BIFROST" -> decryptBifrost(payloadBytes, subtoken)
            "FAFNIR" -> decryptFafnir(payloadBytes, subtoken)
            "FENRIR" -> decryptFenrir(payloadBytes, subtoken)
            "FREYA" -> decryptFreya(payloadBytes, subtoken)
            "GJALLARHORN" -> decryptGjallarhorn(payloadBytes, subtoken)
            "GUNGNIR" -> decryptGungnir(payloadBytes, subtoken)
            "HEIMDALL" -> decryptHeimdall(payloadBytes, subtoken)
            "JORMUNGANDR" -> decryptJormungandr(payloadBytes, subtoken)
            "LOKI" -> decryptLoki(payloadBytes, subtoken)
            "NIDHOGG" -> decryptNidhogg(payloadBytes, subtoken)
            "ODIN" -> decryptOdin(payloadBytes, subtoken)
            "RAGNAROK" -> decryptRagnarok(payloadBytes, subtoken)
            "SKADI" -> decryptSkadi(payloadBytes, subtoken)
            "SLEIPNIR" -> decryptSleipnir(payloadBytes, subtoken)
            "THOR" -> decryptThor(payloadBytes, subtoken)
            "YMIR" -> decryptYmir(payloadBytes, subtoken)
            else -> error("Unsupported cipher: ${encryptedEphemeralKey.cipher}")
        }
    }

    /**
     * Decodes a Base64 string and applies XOR byte-by-byte with the given key.
     * Equivalent to JavaScript's undecode function:
     * - atob(encoded) decodes Base64 to a binary string
     * - charCodeAt gets the byte value (0-255) of each character
     * - XOR with key's charCodeAt values
     * - String.fromCharCode converts back to string
     */
    private fun decode(encoded: String, key: String): String {
        // Decode Base64 to bytes (equivalent to atob in JavaScript)
        val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)

        // Convert key string to bytes using ISO_8859_1 to preserve byte values 0-255
        // This matches JavaScript's charCodeAt behavior where each char represents a byte
        val keyBytes = key.toByteArray(Charsets.ISO_8859_1)

        val result = CharArray(decodedBytes.size)

        for (i in decodedBytes.indices) {
            // XOR between decoded byte and key byte (using modulo to repeat key)
            val decodedByte = decodedBytes[i].toInt() and 0xFF
            val keyByte = keyBytes[i % keyBytes.size].toInt() and 0xFF
            val xorResult = decodedByte xor keyByte
            // Convert to Char (0-65535 range, same as String.fromCharCode in JS)
            result[i] = xorResult.toChar()
        }

        return String(result)
    }

    /**
     * Decrypts chapter data following the JavaScript flow:
     * 1. Deciphers the ephemeral key using the subtoken
     * 2. Decodes `encryptedImageKey` using the ephemeral key
     * 3. Decodes `encryptedUrls` using the result from step 2
     * 4. Parses JSON and returns a list of URLs
     */
    fun decrypt(
        data: SakuraMangaChapterReadDataDto,
        subtoken: String,
    ): List<String> {
        // Step 1: Decipher ephemeral key (equivalent to YggdrasilDecipher.decipher)
        val ephemeralKey = decipher(data.encryptedEphemeralKey, subtoken)

        // Step 2: Decode encryptedImageKey using ephemeral key
        val imageKey = decode(data.encryptedImageKey, ephemeralKey)

        // Step 3: Decode encryptedUrls using imageKey
        val urlsJson = decode(data.encryptedUrls, imageKey)

        // Step 4: Parse JSON and return list of strings (URLs)
        return urlsJson.parseAs<List<String>>()
    }
}
