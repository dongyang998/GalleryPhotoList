package com.example.dyang.galleryphotolist;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * Encapsulate the LruCache for images cache
 * This LruCache will use 1/3 available application memory
 */
public class ImagesCache {

    private static LruCache<String, Bitmap> sImages;
    private static int sMaxSize;

    // initialize the LruCache
    static {

        Runtime rt = Runtime.getRuntime();
        try {
            // use 1/3 available memory size
            sMaxSize = (int)(rt.maxMemory() / 4);
        } catch (Exception e) {
            sMaxSize = -1;
        }

        if (sMaxSize == -1) {
            if (GlobalSettings.Debug) {
               Log.e("ImagesCache", "Can't get the memory size of this application.");
            }
        }

        if (GlobalSettings.Debug) {
            Log.e("ImagesCache", "The maximum size is " + String.valueOf(sMaxSize));
        }

        sImages = initCache(sMaxSize);
    }

    // get image
    public static Bitmap get(String imageID) {
        return sImages.get(imageID);
    }

    // cache image
    public static Bitmap put(String imageID, Bitmap image) {
        return sImages.put(imageID, image);
    }

    public static void evilAll() {
        sImages.evictAll();
    }

    private static LruCache<String, Bitmap> initCache(int maxSize) {
        return new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    // trim the size of cache
    public static void trimHalfCache() {
        // max size > 50M
        if (sMaxSize > 50 * 1024 * 1024) {
            sMaxSize = sMaxSize / 2;
            sImages.evictAll();
            sImages = initCache(sMaxSize);
        }
    }







}
