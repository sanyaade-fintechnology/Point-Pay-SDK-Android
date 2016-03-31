package de.payleven.payment.example;

import android.support.annotation.Nullable;

import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;

/**
 * Defines method that should be implemented by activity in order to communicate with fragments
 */
public interface FragmentInteractionListener {
    void showDeviceManagementScreen();
    void configurePaylevenApi(String userName, String password);
    void changeConfiguration();
    @Nullable
    Payleven getPaylevenApiOrConfigure();
    void setDefaultDevice(PairedDevice device);
    PairedDevice getDefaultDevice();
}
