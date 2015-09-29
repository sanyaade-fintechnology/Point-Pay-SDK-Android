package de.payleven.payment.example.payment;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Simple singleton to preserve an image across activities. In real apps LRU cache may be
 * used or general mechanism to share/cache images.
 */
public class ImageCache {
    private static final class InstanceHolder {
        private static final ImageCache INSTANCE = new ImageCache();
    }

    public static ImageCache getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private Bitmap mCachedImage;

    private ImageCache() {
    }

    /**
     * Sets bitmap of the signature received from customer
     *
     * @param bitmap image to set
     */
    public synchronized void setImage(Bitmap bitmap) {
        mCachedImage = bitmap;
    }

    /**
     * Returns the latest preserved images and sets the cached image to null
     *
     * @return cached image or null
     */
    @Nullable
    public synchronized Bitmap getAndClearImage() {
        Bitmap bitmapToReturn = mCachedImage;
        mCachedImage = null;

        return bitmapToReturn;
    }

    /**
     * Returns the latest preserved image
     *
     * @return cached image or null
     */
    @Nullable
    public synchronized Bitmap get() {
        return mCachedImage;
    }
}
