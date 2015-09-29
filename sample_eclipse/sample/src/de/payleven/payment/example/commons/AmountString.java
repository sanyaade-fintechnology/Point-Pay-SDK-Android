package de.payleven.payment.example.commons;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Formats string with the provided currency
 */
public class AmountString {
    private static final String EUR = "EUR";
    private static final String GBP = "GBP";
    private static final String PLN = "PLN";

    private final Currency mCurrency;
    private final BigDecimal mAmount;

    public AmountString(BigDecimal amount, Currency currency) {
        this.mCurrency = currency;
        this.mAmount = amount;
    }

    @Override
    public String toString() {
        NumberFormat formatter = getFormatter();
        return formatter.format(mAmount);
    }

    private NumberFormat getFormatter() {
        Locale locale = getLocaleFromCurrency(mCurrency);
        return NumberFormat.getCurrencyInstance(locale);
    }

    private Locale getLocaleFromCurrency(Currency currency) {
        if (GBP.equals(currency.getCurrencyCode())) {
            return Locale.UK;
        } else if (PLN.equals(currency.getCurrencyCode())) {
            return new Locale("pl", "PL");
        } else if(EUR.equals(currency.getCurrencyCode())){
            return Locale.GERMANY;
        }

        throw new IllegalStateException("Tried to use unknown currency " +
                currency.getCurrencyCode());
    }
}
