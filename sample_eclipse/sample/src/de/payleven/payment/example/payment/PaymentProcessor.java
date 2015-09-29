package de.payleven.payment.example.payment;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.Device;
import de.payleven.payment.DevicePreparationListener;
import de.payleven.payment.GeoLocation;
import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;
import de.payleven.payment.PaylevenError;
import de.payleven.payment.PaymentListener;
import de.payleven.payment.PaymentRequest;
import de.payleven.payment.PaymentResult;
import de.payleven.payment.PaymentState;
import de.payleven.payment.PaymentTask;
import de.payleven.payment.SignatureResponseHandler;
import de.payleven.payment.example.DefaultDeviceProvider;
import de.payleven.payment.example.payment.SalePayment.State;

/**
 * Runs payment process which includes device preparation and starting payment task.
 * Communicates with UI throw {@link PaymentProcessingView} interface.
 */
public class PaymentProcessor {
    //NOTE: THIS IS ONLY FOR DEBUGGING. Use real device location instead
    private static final GeoLocation CURRENT_LOCATION = new GeoLocation(52.5075419, 13.4261419);

    private final Payleven mPayleven;
    private final DefaultDeviceProvider mDefaultDeviceProvider;
    private final PaymentRepository mPaymentRepository;

    private PaymentTask mPaymentTask;
    private boolean mCancelRequested;
    private SignatureResponseHandler mSignatureResponseHandler;
    private PaymentProcessingView mView;

    public PaymentProcessor(Payleven payleven,
                            DefaultDeviceProvider mDefaultDeviceProvider,
                            PaymentRepository paymentRepository) {
        this.mPayleven = payleven;
        this.mDefaultDeviceProvider = mDefaultDeviceProvider;
        this.mPaymentRepository = paymentRepository;
    }

    public void bindView(PaymentProcessingView view) {
        mView = view;
    }

    public void unbindView() {
        mView = null;
    }

    /**
     * Prepares the default paired device for payment. The device needs to be successfully
     * prepared to proceed with the payment.
     */
    public void startPayment(@NonNull String paymentId,
                             @NonNull BigDecimal amount,
                             @NonNull Currency currency) {
        mCancelRequested = false;
        prepareDeviceForPayment(paymentId, amount, currency);
    }

    /**
     * Attempts to cancel a payment.
     */
    public void cancel() {
        mCancelRequested = true;
        if (mPaymentTask != null) {
            mPaymentTask.cancel();
            mPaymentTask = null;
        }
    }

    /**
     * Signature is confirmed by the merchant.
     */
    public void confirmSignature(Bitmap signature) {
        if (mSignatureResponseHandler != null) {
            mSignatureResponseHandler.confirmSignature(signature);
            mSignatureResponseHandler = null;
        }
    }

    /**
     * Signature is declined by the merchant.
     */
    public void declineSignature(Bitmap signature) {
        if (mSignatureResponseHandler != null) {
            mSignatureResponseHandler.declineSignature(signature);
            mSignatureResponseHandler = null;
        }
    }

    private void prepareDeviceForPayment(@NonNull final String paymentId,
                                         @NonNull final BigDecimal amount,
                                         @NonNull final Currency currency) {

        PairedDevice defaultDevice = mDefaultDeviceProvider.get(mPayleven);
        if (defaultDevice == null) {
            notifyDefaultDeviceNotSet();
        } else {
            DevicePreparationListener listener = new DevicePreparationListenerImpl(
                    mPayleven, paymentId, amount, currency);
            mPayleven.prepareDevice(defaultDevice, listener);
        }
    }

    private void startPaymentWithDevice(@NonNull Payleven api,
                                        @NonNull String paymentId,
                                        @NonNull BigDecimal amount,
                                        @NonNull Currency currency,
                                        @NonNull Device device) {
        try {
            mPaymentTask = createPaymentTask(api, paymentId, amount, currency, device);
            mPaymentTask.startAsync(new PaymentListenerImpl());
        } catch (IllegalArgumentException e) {
            mView.onError(e.getMessage());
        }
    }

    private PaymentTask createPaymentTask(@NonNull Payleven api,
                                          @NonNull String paymentId,
                                          @NonNull BigDecimal amount,
                                          @NonNull Currency currency,
                                          @NonNull Device defaultDevice) {
        //Use real geo location instead of the hardcoded one
        GeoLocation currentLocation = CURRENT_LOCATION;

        PaymentRequest paymentRequest = new PaymentRequest(amount, currency, paymentId,
                currentLocation);

        return api.createPaymentTask(paymentRequest, defaultDevice);
    }

    private void notifyDefaultDeviceNotSet() {
        if (mView != null) {
            mView.onDefaultDeviceNotSet();
        }
    }

    private void notifyError(String message) {
        if (mView != null) {
            mView.onError(message);
        }
    }

    private void notifySignatureRequested() {
        if (mView != null) {
            mView.onSignatureRequested();
        }
    }

    private void notifyPaymentCompleted(PaymentResult paymentResult) {
        if (mView != null) {
            mView.onPaymentComplete(paymentResult);
        }
    }

    private void notifyCanceledBeforePaymentStart() {
        if (mView != null) {
            mView.onCanceledBeforePaymentStart();
        }
    }

    private void storePaymentResult(PaymentResult result) {
        final SalePayment payment = new SalePayment(result.getId(),
                result.getDate(),
                result.getAmount(),
                result.getCurrency(),
                result.getState() == PaymentState.APPROVED ? State.APPROVED : State.FAILED);

        mPaymentRepository.addPayment(payment);
    }

    /**
     * Prepares payleven bluetooth device and starts payment in case of success.
     */
    private class DevicePreparationListenerImpl implements DevicePreparationListener {
        private final Payleven mPaylevenApi;
        private final String mPaymentId;
        private final BigDecimal mAmount;
        private final Currency mCurrency;

        private DevicePreparationListenerImpl(@NonNull Payleven api,
                                              @NonNull String paymentId,
                                              @NonNull BigDecimal amount,
                                              @NonNull Currency currency) {
            this.mPaylevenApi = api;
            this.mPaymentId = paymentId;
            this.mAmount = amount;
            this.mCurrency = currency;
        }

        /**
         * Device pairing was successful.
         * Proceed with payment if it was not cancelled by the merchant.
         */
        @Override
        public void onDone(Device device) {
            if (mCancelRequested) {
                notifyCanceledBeforePaymentStart();
            } else {
                startPaymentWithDevice(mPaylevenApi, mPaymentId, mAmount, mCurrency, device);
            }
        }

        /**
         * Device pairing was unsuccessful. Show error message.
         */
        @Override
        public void onError(PaylevenError error) {
            notifyError(error.getMessage());
        }
    }

    /**
     * Reacts on the payment completion, on an error during the payment process
     * or if a signature is needed to complete the payment.
     */
    private class PaymentListenerImpl implements PaymentListener {
        @Override
        public void onPaymentComplete(PaymentResult result) {
            mPaymentTask = null;
            storePaymentResult(result);
            notifyPaymentCompleted(result);
        }

        @Override
        public void onSignatureRequested(SignatureResponseHandler signatureHandler) {
            mSignatureResponseHandler = signatureHandler;
            notifySignatureRequested();
        }

        @Override
        public void onError(PaylevenError error) {
            int errorCode = error.getCode();
            if (errorCode == 0) {
                notifyError(error.getMessage());
            } else {
                notifyError("Error " + errorCode + "\n" + error.getMessage());
            }
        }
    }

}
