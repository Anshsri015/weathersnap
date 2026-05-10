package com.example.weathersnap.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    private const val COMPRESSION_QUALITY = 70   // 70% JPEG quality
    private const val TAG = "ImageCompressor"

    /**
     * Compresses [originalFile] at [COMPRESSION_QUALITY]% JPEG quality.
     *
     * @return Pair(compressedFilePath, compressedSizeBytes)
     *         Falls back to original file if compression fails.
     */
    fun compress(context: Context, originalFile: File): Pair<String, Long> {
        return try {
            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
                ?: return Pair(originalFile.absolutePath, originalFile.length())

            val compressedFile = File(context.filesDir, "compressed_${originalFile.name}")

            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
            }

            bitmap.recycle()

            Log.d(TAG, "Original: ${originalFile.length()} B → Compressed: ${compressedFile.length()} B")
            Pair(compressedFile.absolutePath, compressedFile.length())

        } catch (e: Exception) {
            Log.e(TAG, "Compression failed, using original", e)
            Pair(originalFile.absolutePath, originalFile.length())
        }
    }
}
