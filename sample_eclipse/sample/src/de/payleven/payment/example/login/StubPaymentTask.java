package de.payleven.payment.example.login;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import de.payleven.payment.PaylevenError;
import de.payleven.payment.PaymentListener;
import de.payleven.payment.PaymentProgressListener;
import de.payleven.payment.PaymentProgressState;
import de.payleven.payment.PaymentResult;
import de.payleven.payment.PaymentTask;
import de.payleven.payment.SignatureRequestListener;
import de.payleven.payment.SignatureResponseHandler;

/**
 * Stub version of {@link PaymentTask}.
 */
public class StubPaymentTask implements PaymentTask {
    public static final int DEFAULT_TIMEOUT = 2000;
    private final PaylevenError mError;
    private final PaymentResult mResult;
    private final boolean isSignatureRequested;

    public static PaymentTask createWithSignature(PaymentResult result) {
        return new StubPaymentTask(result, true, null);
    }

    public static PaymentTask create(PaymentResult result) {
        return new StubPaymentTask(result, false, null);
    }

    public static PaymentTask createWithError(PaylevenError error) {
        return new StubPaymentTask(null, false, error);
    }

    private StubPaymentTask(PaymentResult result,
                            boolean isSignatureRequested,
                            PaylevenError error) {
        this.mResult = result;
        this.isSignatureRequested = isSignatureRequested;
        this.mError = error;
    }

    @Override
    public void startAsync(final PaymentListener listener) {
        if (mError != null) {
            listener.onError(mError);
        } else {
            handleSuccessCase(listener);
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public PaymentResult startAndWait(SignatureRequestListener signatureListener,
                                      PaymentProgressListener paymentProgressListener) {
        return null;
    }

    @Override
    public PaymentProgressState getPaymentProgressState() {
        return null;
    }

    private void handleSuccessCase(PaymentListener listener) {
        if (isSignatureRequested) {
            requestSignature(listener);
        } else {
            triggerCompleteCallback(listener, DEFAULT_TIMEOUT);
        }
    }

    private void requestSignature(final PaymentListener listener) {
        listener.onSignatureRequested(new SignatureResponseHandler() {
            @Override
            public void confirmSignature(Bitmap signatureImage) {
                triggerCompleteCallback(listener, DEFAULT_TIMEOUT);
            }

            @Override
            public void declineSignature(Bitmap signatureImage) {
                listener.onPaymentComplete(mResult);
            }
        });
    }

    private void triggerCompleteCallback(final PaymentListener listener, final long timeout) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void aVoid) {
                listener.onPaymentComplete(mResult);
            }
        }.execute();
    }

}
