package com.codingspezis.android.metalonly.player.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import com.codingspezis.android.metalonly.player.R
import com.codingspezis.android.metalonly.player.StreamControlActivity

import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

/**
 * A file cache based on [https://github.com/thest1/LazyList](https://github.com/thest1/LazyList). It
 * was modified to work with internal storage and a cache duration of 1 week. We don't use URLs
 * anymore but moderator names (as this is what the cache is for). We also added some synchornized
 * statements.
 *
 * @todo Can "isTooOld" etc. use regular file attributes?
 *
 * @todo Why were the synchronized statements necessary?
 */
class FileCache(private val context: Context) {

    @Synchronized @Throws(FileNotFoundException::class)
    fun getOutputStream(moderator: String): FileOutputStream {
        val fileName = computeFileName(moderator)
        if (hasThumb(context, fileName)) {
            context.deleteFile(fileName)
        }

        val editor = context.getSharedPreferences(context.getString(R.string.app_name), 0).edit()
        editor.putLong(StreamControlActivity.KEY_SP_MODTHUMBDATE + fileName, Calendar
                .getInstance().timeInMillis)
        editor.apply()

        return context.openFileOutput(fileName, Context.MODE_PRIVATE)
    }

    /**
     * decodes image and scales it to reduce memory consumption
     * @param moderator
     * *
     * @return
     */
    @Synchronized operator fun get(moderator: String?): Bitmap? {
        if (moderator == null) {
            /* Java interop. We do not know for sure that moderator is != null */
            return null
        }

        val fileName = computeFileName(moderator)

        if (hasThumb(context, moderator)) {
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

    fun clear() {
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
         * checks if thumb of getModerator is already loaded
         * @param context context of private storage
         * @param moderator The moderator's name
         * @return file name of thumb if exists - null otherwise
         */
        fun hasThumb(context: Context, moderator: String): Boolean =
                context.fileList().contains(computeFileName(moderator))

        /**
         * Computes a hash of the moderator's name to be used as filename.
         */
        private fun computeFileName(moderator: String) = moderator

        /**
         * checks cache duration of special file
         * @param context context for shared preferences
         * @param fileName name of file
         * @return true if file is older than cache duration
         */
        fun isTooOld(context: Context, fileName: String): Boolean {
            val settings = context.getSharedPreferences(context.getString(R.string.app_name), 0)
            val modThumDate = settings.getLong(StreamControlActivity.KEY_SP_MODTHUMBDATE + fileName, 0)
            val currentDate = Calendar.getInstance().timeInMillis

            return currentDate - modThumDate > WEEK_IN_MILLISECS
        }

        @Synchronized fun clear(context: Context) {
            val files = context.fileList()
            for (file in files) {
                if (isTooOld(context, file)) {
                    context.deleteFile(file)
                }
            }
        }
    }

}