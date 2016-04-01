package de.payleven.payment.example.login;

import android.app.Activity;
import android.content.Intent;

import de.payleven.payment.example.StartupActivity;
import de.payleven.payment.example.payment.PaymentRepository;

/**
 * Allows to finish the specified activity in order to display login screen.
 */
public class LogoutController {
    private final PaylevenProvider mPaylevenProvider;

    public LogoutController(PaylevenProvider paylevenProvider) {
        mPaylevenProvider = paylevenProvider;
    }

    public void logoutFrom(Activity activity) {
        mPaylevenProvider.reset();
        Intent intent = new Intent(activity, StartupActivity.class);
        activity.startActivity(intent);
        activity.finish();

    }
}