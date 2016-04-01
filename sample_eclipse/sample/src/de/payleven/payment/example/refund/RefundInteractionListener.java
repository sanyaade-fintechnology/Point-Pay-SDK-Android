package de.payleven.payment.example.refund;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.PaymentState;

/**
 * Used for communication between refund activity and fragments.
 */
public interface RefundInteractionListener {
    void processRefund(BigDecimal amount,
                       Currency currency,
                       String originalPaymentId,
                       String refundId);

    void showRefundCompletedScreen();

    void showRefundFragment();
}
