package de.payleven.payment.example;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.Toast;

import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;
import de.payleven.payment.PaylevenError;
import de.payleven.payment.PaylevenFactory;
import de.payleven.payment.PaylevenRegistrationListener;

public class MainActivity extends Activity implements FragmentInteractionListener {
    private static final String TAG_DATA_FRAGMENT = "data_fragment_tag";
    private static final String PREFERENCES_NAME = "preferences";
    private static final String DEFAULT_DEVICE_ID = "default_device_id";
    //Use your api key here
    private static final String API_KEY = "01a4ac1954c3408caca76ba087ae2510";

    private PaylevenRegistrationListenerImpl registrationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            addDataFragment(null);
            showConfigurationFragment();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registrationListener != null) {
            registrationListener.release();
            hideProgress();
        }
    }

    @Override
    public void configurePaylevenApi(String userName, String password) {
        if (registrationListener != null) {
            registrationListener.release();
        }

        showProgress();
        registrationListener = new PaylevenRegistrationListenerImpl(this);
        PaylevenFactory.registerAsync(getApplicationContext(),
                userName,
                password,
                API_KEY,
                registrationListener);
    }

    @Override
    public void changeConfiguration() {
        setPaylevenApi(null);
        setDefaultDevice(null);
        showConfigurationFragment();
    }

    @Override
    public void showDeviceManagementScreen() {
        Fragment fragment = new DeviceManagementFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public Payleven getPaylevenApiOrConfigure() {
        Payleven paylevenApi = retrievePaylevenApi();

        if (paylevenApi != null) {
            return paylevenApi;
        }

        showConfigurationFragment();

        return null;
    }

    @Override
    public void setDefaultDevice(@Nullable PairedDevice device) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        preferences.edit()
                .putString(DEFAULT_DEVICE_ID, device != null ? device.getId() : null)
                .apply();
    }

    @Override
    @Nullable
    public PairedDevice getDefaultDevice() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        String deviceId = preferences.getString(DEFAULT_DEVICE_ID, null);

        if (deviceId == null) {
            return null;
        }

        Payleven paylevenApi = getPaylevenApiOrConfigure();
        if (paylevenApi != null) {
            for (PairedDevice item : paylevenApi.getPairedDevices()) {
                if (item.getId().equals(deviceId)) {
                    return item;
                }
            }
        }
        //Clear default device if it wasn't found in the list of registered devices
        preferences.edit().putString(DEFAULT_DEVICE_ID, null).apply();

        return null;
    }

    private Payleven retrievePaylevenApi() {
        DataFragment dataFragment = (DataFragment) getFragmentManager()
                .findFragmentByTag(TAG_DATA_FRAGMENT);
        return dataFragment.getPaylevenApi();
    }

    private void setPaylevenApi(Payleven paylevenApi) {
        DataFragment dataFragment = (DataFragment) getFragmentManager()
                .findFragmentByTag(TAG_DATA_FRAGMENT);
        dataFragment.setPaylevenApi(paylevenApi);
    }

    private void addDataFragment(Payleven paylevenApi) {
        DataFragment fragment = new DataFragment();
        fragment.setPaylevenApi(paylevenApi);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(fragment, TAG_DATA_FRAGMENT);
        ft.commit();
    }

    private void showConfigurationFragment() {
        Fragment configFragment = new ConfigurationFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, configFragment);
        ft.commit();
    }

    private void showPaymentFragment() {
        Fragment fragment = new PaymentFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    private void showProgress() {
        setProgressBarIndeterminateVisibility(true);
    }

    private void hideProgress() {
        setProgressBarIndeterminateVisibility(false);
    }

    private void showError(final String error) {
        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
    }

    /**
     * Used to preserve activity scoped objects
     */
    public static final class DataFragment extends Fragment {
        private Payleven mPaylevenApi;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        private void setPaylevenApi(Payleven paylevenApi) {
            mPaylevenApi = paylevenApi;
        }

        private Payleven getPaylevenApi() {
            return mPaylevenApi;
        }
    }

    /**
     * Reacts on the result of registration task. Callback methods are executed on the main thread
     */
    private static class PaylevenRegistrationListenerImpl implements PaylevenRegistrationListener {
        private MainActivity activity;

        private PaylevenRegistrationListenerImpl(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onRegistered(Payleven payleven) {
            if (activity != null) {
                activity.setPaylevenApi(payleven);
                activity.showPaymentFragment();
                activity.hideProgress();
            }
        }

        @Override
        public void onError(PaylevenError error) {
            if (activity != null) {
                activity.showError(error.getMessage());
                activity.hideProgress();
            }
        }

        /**
         * Must be called when activity is dismissed in order to avoid memory leaks
         */
        protected void release() {
            activity = null;
        }
    }
}
