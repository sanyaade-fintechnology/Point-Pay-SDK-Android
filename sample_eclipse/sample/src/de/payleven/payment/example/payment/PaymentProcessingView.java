package de.payleven.payment.example.payment;

import de.payleven.payment.PaymentProgressState;
import de.payleven.payment.PaymentResult;

/**
 * Defines callback methods for communication between {@link PaymentProcessor} and the view.
 */
public interface PaymentProcessingView {
    /**
     * Called when payment is initiated but default devie is not selected in the "Terminals"
     * section of the app
     */
    void onDefaultDeviceNotSet();

    /**
     * Notifies about any error during the payment.
     */
    void onError(String message);

    /**
     * Notifies that the customer should provide his signature.
     */
    void onSignatureRequested();

    /**
     * Called when final payment status is received.
     */
    void onPaymentComplete(PaymentResult paymentResult);

    /**
     * Called when user cancels during the device preparation.
     */
    void onCanceledBeforePaymentStart();

    /**
     * Called when the payment progress state changed.
     */
    void onPaymentProgressStateChanged(PaymentProgressState paymentProgressState);
}
