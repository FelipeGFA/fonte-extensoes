package eu.kanade.tachiyomi.extension.pt.argosscan

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import tachiyomi.decoder.ImageDecoder
import kotlin.math.roundToInt

internal fun String.toThumbnailUrl(): String? = runCatching {
    toHttpUrl().newBuilder()
        .addQueryParameter(THUMBNAIL_QUERY_PARAMETER, THUMBNAIL_QUERY_VALUE)
        .build()
        .toString()
}.getOrNull()

internal class ThumbnailInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.queryParameter(THUMBNAIL_QUERY_PARAMETER) != THUMBNAIL_QUERY_VALUE) {
            return chain.proceed(request)
        }

        val originalUrl = request.url.newBuilder()
            .removeAllQueryParameters(THUMBNAIL_QUERY_PARAMETER)
            .build()
        val originalRequest = request.newBuilder().url(originalUrl).build()
        val response = chain.proceed(originalRequest)
        if (!response.isSuccessful) return response

        return response.use {
            it.toThumbnailResponse()
        }
    }

    private fun Response.toThumbnailResponse(): Response {
        val bitmap = decodeThumbnailBitmap() ?: run {
            return emptyThumbnailResponse()
        }

        val scaledBitmap = try {
            bitmap.scaleToMaxSize(THUMBNAIL_MAX_SIZE)
        } catch (_: RuntimeException) {
            bitmap.recycle()
            return emptyThumbnailResponse()
        } catch (_: OutOfMemoryError) {
            bitmap.recycle()
            return emptyThumbnailResponse()
        }

        return try {
            asJpegResponse(scaledBitmap)
        } catch (_: RuntimeException) {
            emptyThumbnailResponse()
        } catch (_: OutOfMemoryError) {
            emptyThumbnailResponse()
        } finally {
            if (scaledBitmap !== bitmap) scaledBitmap.recycle()
            bitmap.recycle()
        }
    }

    private fun Response.emptyThumbnailResponse(): Response {
        val bitmap = emptyThumbnail()
        return try {
            asJpegResponse(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private fun Response.asJpegResponse(bitmap: Bitmap): Response {
        val buffer = Buffer()
        buffer.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, output)
        }

        return newBuilder()
            .removeHeader("Content-Encoding")
            .header("Content-Type", JPEG_MEDIA_TYPE.toString())
            .header("Content-Length", buffer.size.toString())
            .body(buffer.asResponseBody(JPEG_MEDIA_TYPE, buffer.size))
            .build()
    }

    private fun Bitmap.scaleToMaxSize(maxSize: Int): Bitmap {
        val largestSide = maxOf(width, height)
        if (largestSide <= maxSize) return this

        val scale = maxSize.toFloat() / largestSide
        val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    private fun Response.decodeThumbnailBitmap(): Bitmap? {
        val decoder = try {
            ImageDecoder.newInstance(body.byteStream())
        } catch (_: RuntimeException) {
            null
        } catch (_: OutOfMemoryError) {
            null
        } ?: return null

        return try {
            val sampleSize = decoder.thumbnailSampleSize()
            val bitmap = decoder.decode(
                Rect(0, 0, decoder.width, decoder.height),
                sampleSize,
            )
            bitmap
        } catch (_: RuntimeException) {
            null
        } catch (_: OutOfMemoryError) {
            null
        } finally {
            decoder.recycle()
        }
    }

    private fun ImageDecoder.thumbnailSampleSize(): Int {
        val targetSize = THUMBNAIL_MAX_SIZE * THUMBNAIL_DECODE_OVERSAMPLE
        val largestSide = maxOf(width, height)
        var sampleSize = 1

        while (largestSide / (sampleSize * 2) >= targetSize) {
            sampleSize *= 2
        }

        return sampleSize
    }

    private fun emptyThumbnail() = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565).apply {
        eraseColor(Color.WHITE)
    }
}

private const val THUMBNAIL_QUERY_PARAMETER = "argos_thumb"
private const val THUMBNAIL_QUERY_VALUE = "1"
private const val THUMBNAIL_MAX_SIZE = 2560
private const val THUMBNAIL_DECODE_OVERSAMPLE = 1
private const val THUMBNAIL_QUALITY = 100

private val JPEG_MEDIA_TYPE = "image/jpeg".toMediaType()
