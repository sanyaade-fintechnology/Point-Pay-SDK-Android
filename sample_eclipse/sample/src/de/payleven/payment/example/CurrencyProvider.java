package de.payleven.payment.example;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.Currency;

/**
 * Allows to set and get a currency.
 */
public class CurrencyProvider {
    private static final String PREFERENCES_NAME = "preferences";
    private static final String DEFAULT_DEVICE_ID = "default_currency_code";
    public static final String DEFAULT_CURRENCY = "EUR";

    private final Context mContext;

    public CurrencyProvider(Context context) {
        mContext = context;
    }

    public void setCurrency(Currency currency) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        preferences.edit().putString(DEFAULT_DEVICE_ID, currency.getCurrencyCode()).apply();
    }

    @Nullable
    public Currency getCurrency() {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        String currencyCode = preferences.getString(DEFAULT_DEVICE_ID, DEFAULT_CURRENCY);

        return Currency.getInstance(currencyCode);
    }
}
