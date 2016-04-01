package de.payleven.payment.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import de.payleven.payment.Payleven;
import de.payleven.payment.example.login.PaylevenProvider;
import de.payleven.payment.example.payment.PaymentActivity;

/**
 * Entry point of the app.
 */
public class StartupActivity extends Activity {

    private PaylevenProvider mPaylevenProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectPaylevenProvider();

        /*
            Waits until we have an instance of Payleven in order to proceed to payment screen.
            The instance will be returned immediately to the callback if we have logged in
            previously. Otherwise a login screen will be shown.
         */
        mPaylevenProvider.getPayleven(new PaylevenProvider.Callback() {
            @Override
            public void onDone(@NonNull Payleven payleven) {
                showHomeScreen();
            }

            @Override
            public void onCanceled() {
                finish();
            }
        });
    }

    /**
     * This method may be substituted with the real injection mechanism like Dagger.
     */
    private void injectPaylevenProvider() {
        mPaylevenProvider = ((SampleApplication) getApplication()).getPaylevenProvider();
    }

    private void showHomeScreen() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
