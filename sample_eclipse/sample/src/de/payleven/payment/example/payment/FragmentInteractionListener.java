package de.payleven.payment.example.payment;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.PaymentState;

/**
 * Defines method that should be implemented by activity in order to communicate with fragments
 */
public interface FragmentInteractionListener {
    void processPayment(BigDecimal amount, Currency currency, String externalId);
    void showPaymentFragment();
    void setBackNavigationEnabled(boolean enabled);
}
