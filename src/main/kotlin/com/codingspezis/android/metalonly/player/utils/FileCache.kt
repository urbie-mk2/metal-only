package com.codingspezis.android.metalonly.player.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import com.codingspezis.android.metalonly.player.R
import com.codingspezis.android.metalonly.player.StreamControlActivity
import java.io.File

import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

/**
 * A file cache based on [https://github.com/thest1/LazyList](https://github.com/thest1/LazyList). It
 * was modified to work with internal storage and a cache duration of 1 week. We don't use URLs
 * anymore but moderator names (as this is what the cache is for). We also added some synchronized
 * statements.
 *
 * @todo Why were the synchronized statements necessary?
 */
class FileCache(private val context: Context) : Cache {
    init {
        removeDeprecatedPrefCache()
    }

    /**
     * Clears the previously used pref values for caching. The pref values contained the name of the
     * moderator and the lastModified time; they are not used anymore.
     *
     * @todo Remove in 0.7 or 0.8
     */
    private fun removeDeprecatedPrefCache() {
        val preferences = context.getSharedPreferences(context.getString(R.string.app_name), 0)
        val editor = preferences.edit()
        preferences.all.keys
                .filter { it.startsWith(StreamControlActivity.KEY_SP_MODTHUMBDATE) }
                .forEach { editor.remove(it) }
        editor.apply()
    }

    @Synchronized @Throws(FileNotFoundException::class)
    fun getOutputStream(moderator: String): FileOutputStream {
        return context.openFileOutput(moderator, Context.MODE_PRIVATE)
    }

    override fun set(moderator: String, newBitmap: Bitmap?) {
        val outputStream = getOutputStream(moderator)
        newBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
    }

    /**
     * decodes image and scales it to reduce memory consumption
     * @param moderator
     * *
     * @return
     */
    @Synchronized override operator fun get(moderator: String?): Bitmap? {
        if (moderator == null) {
            /* Java interop. We do not know for sure that moderator is != null */
            return null
        }

        val fileName = moderator

        if (existsAndIsRecent(context, moderator)) {
            try {

                val options = BitmapFactory.Options()
                options.inSampleSize = scalingFactor(fileName)

                val inputStream = context.openFileInput(fileName)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()

                return bitmap
            } catch (e: IOException) {
                Log.e(TAG, e.message, e)
            }

        }
        return null
    }

    private fun scalingFactor(fileName: String): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        val inputStream = context.openFileInput(fileName)
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        var scalingFactor = 1
        while (options.outWidth / 2 < MIN_SIZE || options.outHeight / 2 < MIN_SIZE) {
            options.outWidth = options.outWidth / 2
            options.outHeight = options.outHeight / 2
            scalingFactor *= 2
        }
        return scalingFactor
    }

    override fun clear() {
        clear(context)
    }
    companion object {
        val WEEK_IN_MILLISECS = (7 * 24 * 60 * 60 * 1000).toLong()

        /**
         * The minimum width and height for images
         */
        val MIN_SIZE = 64

        private val TAG = FileCache::class.java.simpleName

        /**
         * checks cache duration of special file
         * @param context context for shared preferences
         * @param fileName name of file
         * @return true if file exists and is recent enough
         */
        fun existsAndIsRecent(context: Context, fileName: String): Boolean {
            val file = File("${context.filesDir.absolutePath}/$fileName")
            val currentTimeInMillis = Calendar.getInstance().timeInMillis
            val timeSinceLastModification = currentTimeInMillis - file.lastModified()

            return timeSinceLastModification < WEEK_IN_MILLISECS
        }

        @Synchronized fun clear(context: Context) {
            val files = context.fileList()
            for (file in files) {
                context.deleteFile(file)
            }
        }
    }
}