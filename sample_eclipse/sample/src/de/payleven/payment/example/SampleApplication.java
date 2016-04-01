package de.payleven.payment.example;

import android.app.Application;

import de.payleven.payment.Payleven;
import de.payleven.payment.example.login.LogoutController;
import de.payleven.payment.example.login.PaylevenProvider;
import de.payleven.payment.example.payment.PaymentRepository;
import de.payleven.payment.example.payment.SharedPreferencesPaymentRepository;

/**
 * Base class to hold sample application state.
 */
public class SampleApplication extends Application {
    private Payleven mPaylevenApi;

    private PaylevenProvider mPaylevenProvider;
    private PaymentRepository mPaymentRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        mPaylevenProvider = new PaylevenProvider(this);
        //You may want to substitute the real implementation with the stubbed one for testing
        // purposes.
        //For example:
        //mPaylevenProvider = StubPaylevenProvider.alwaysApprovedPaylevenWithSignature(this);
    }

    public void setPayleven(Payleven payleven){
        mPaylevenApi = payleven;
    }

    public Payleven getPayleven(){
        return mPaylevenApi;
    }

    public PaylevenProvider getPaylevenProvider() {
        return mPaylevenProvider;

    }

    public LogoutController getLogoutController() {
        return new LogoutController(mPaylevenProvider);
    }

    public DefaultDeviceProvider getDefaultDeviceProvider() {
        return new DefaultDeviceProvider(this);
    }

    public CurrencyProvider getCurrencyProvider() {
        return new CurrencyProvider(this);
    }

    public PaymentRepository getPaymentRepository(){
        return mPaymentRepository;
    }

    public void setPaymentRepository(String email) {
        mPaymentRepository = new SharedPreferencesPaymentRepository(getApplicationContext(), email);
    }
}
