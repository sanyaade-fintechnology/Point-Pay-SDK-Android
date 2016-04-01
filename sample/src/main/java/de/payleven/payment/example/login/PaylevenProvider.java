package de.payleven.payment.example.login;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.payleven.payment.Payleven;

/**
 * Provides the instance of registered {@link de.payleven.payment.Payleven}.
 */
public class PaylevenProvider {
    private final Context context;
    @Nullable
    private Payleven mPayleven;

    private List<Callback> mCallbacks = new ArrayList<Callback>();

    public interface Callback {
        /**
         * Called when the registration is successful
         */
        void onDone(@NonNull Payleven payleven);

        /**
         * Called if registration was cancelled by user.
         */
        void onCanceled();
    }

    /**
     * Class created for convenience when you do not need to implement
     * {@link Callback#onCanceled()}
     */
    public static abstract class DoneCallback implements Callback{
        @Override
        public final void onCanceled() {
            //Ignored. Use original Callback class if you need to implement onCancelled().
        }
    }

    public PaylevenProvider(Context context) {
        this.context = context;
    }

    public void getPayleven(Callback callback) {
        if (mPayleven != null) {
            callback.onDone(mPayleven);
        } else {
            registerCallback(callback);
            showLoginScreen();
        }
    }

    void update(@NonNull Payleven payleven) {
        mPayleven = payleven;
        notifyAboutSuccess(payleven);
    }

    void reset() {
        mPayleven = null;
        mCallbacks.clear();
    }

    synchronized void notifyAboutCancel() {
        for (Callback callback : mCallbacks) {
            callback.onCanceled();
        }

        mCallbacks.clear();
    }

    private synchronized void registerCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    private void showLoginScreen() {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private synchronized void notifyAboutSuccess(Payleven payleven) {
        for (Callback callback : mCallbacks) {
            callback.onDone(payleven);
        }

        mCallbacks.clear();
    }
}
