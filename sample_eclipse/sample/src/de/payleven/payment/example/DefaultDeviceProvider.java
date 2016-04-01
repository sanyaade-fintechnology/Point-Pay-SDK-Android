package de.payleven.payment.example;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;

/**
 * Allows to set and get paired device for future use.
 */
public class DefaultDeviceProvider {
    private static final String PREFERENCES_NAME = "preferences";
    private static final String DEFAULT_DEVICE_ID = "default_device_id";

    private final Context mContext;

    public DefaultDeviceProvider(Context context) {
        mContext = context;
    }

    public void set(@Nullable PairedDevice device) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        preferences.edit()
                .putString(DEFAULT_DEVICE_ID, device != null ? device.getId() : null)
                .apply();
    }

    @Nullable
    public PairedDevice get(@NonNull Payleven paylevenApi) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        String deviceId = preferences.getString(DEFAULT_DEVICE_ID, null);

        if (deviceId == null) {
            return null;
        }

        for (PairedDevice item : paylevenApi.getPairedDevices()) {
            if (item.getId().equals(deviceId)) {
                return item;
            }
        }

        //Clear default device if it wasn't found in the list of registered devices
        preferences.edit().putString(DEFAULT_DEVICE_ID, null).apply();

        return null;
    }
}
