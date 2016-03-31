package de.payleven.payment.example;

import android.graphics.Bitmap;

/**
 * Simple singleton to preserve signature image across activities. In real apps LRU cache may be
 * used or general mechanism to share/cache images.
 */
public class SignatureCache {
    private static final class InstanceHolder {
        private static final SignatureCache INSTANCE = new SignatureCache();
    }

    public static SignatureCache getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private Bitmap cachedSignature;

    private SignatureCache() {
    }

    /**
     * Sets bitmap of the signature received from customer
     *
     * @param bitmap image to set
     */
    public synchronized void setSignature(Bitmap bitmap) {
        cachedSignature = bitmap;
    }

    /**
     * Returns the latest preserved images and sets the cached image to null
     *
     * @return cached image or null
     */
    public synchronized Bitmap getAndClearSignature() {
        Bitmap bitmapToReturn = cachedSignature;
        cachedSignature = null;

        return bitmapToReturn;
    }
}
