package de.payleven.payment.example.refund;

import de.payleven.payment.RefundResult;
import de.payleven.payment.example.payment.PaymentProcessor;

/**
 * Defines callback methods for communication between {@link RefundProcessor} and the view.
 */
public interface RefundProcessingView {
    /**
     * Notifies about any error during the payment.
     */
    void onError(String message);

    /**
     * Called when final payment status is received.
     */
    void onRefundComplete(RefundResult refundResult);
}
