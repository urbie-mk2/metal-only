package com.codingspezis.android.metalonly.player.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import com.codingspezis.android.metalonly.player.R;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * this class comes in the original form from:
 * https://github.com/thest1/LazyList
 * <p/>
 * it works not longer with URLs but with getModerator names of metal only some
 * synchronized statements are also added
 */
public class ImageLoader {

    final int stub_id = R.drawable.mo_wait;
    private final Map<ImageView, String> imageViews =
            Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    private final MemoryCache memoryCache = new MemoryCache();
    private final FileCache fileCache;
    private final ExecutorService executorService;
    private final Handler handler = new Handler();

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    public void displayImage(String moderator, ImageView imageView) {
        imageViews.put(imageView, moderator);

        Bitmap bitmap = memoryCache.get(moderator);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            queuePhoto(moderator, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String moderator, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(moderator, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    synchronized Bitmap getBitmap(String moderator) {
        Bitmap b = fileCache.decodeMod(moderator);
        if (b != null) {
            return b;
        }

        try {
            URL imageUrl = new URL(UrlConstants.METAL_ONLY_MODERATOR_PIC_BASE_URL + moderator);

            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            InputStream is = conn.getInputStream();
            OutputStream os = fileCache.getOutputStream(moderator);

            Utils.CopyStream(is, os);

            is.close();
            os.close();

            return fileCache.decodeMod(moderator);
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError) {
                memoryCache.clear();
            }
            return null;
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.moderator);
    }

    class PhotoToLoad {
        final String moderator;
        final ImageView imageView;

        PhotoToLoad(String moderator, ImageView imageView) {
            this.moderator = moderator;
            this.imageView = imageView;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad)) {
                    return;
                }
                Bitmap bmp = getBitmap(photoToLoad.moderator);
                memoryCache.put(photoToLoad.moderator, bmp);
                if (imageViewReused(photoToLoad)) {
                    return;
                }
                BitmapDisplayer bd = new BitmapDisplayer(ImageLoader.this, bmp,
                        photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

}
