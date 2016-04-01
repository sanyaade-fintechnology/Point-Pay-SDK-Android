package de.payleven.payment.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.example.R;
import de.payleven.payment.example.commons.AmountString;

/**
 * Allows to display amount with the provided currency.
 */
public class AmountView extends TextView {
    private BigDecimal mAmount = BigDecimal.ZERO;
    private Currency mCurrency;

    public AmountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupViews();
    }

    /**
     * Updates total amount when a number is introduced or deleted
     */
    public void setAmount(BigDecimal amount) {
        mAmount = amount == null ? BigDecimal.ZERO : amount;
        updateAmountView();
    }

    /**
     * Updates currency in the total amount view
     */
    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalStateException("Currency can not be null");
        }
        mCurrency = currency;
        updateAmountView();
    }


    public BigDecimal getAmount() {
        return mAmount;
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    private void setupViews() {
        setSingleLine(true);
        setTextAppearance(getContext(), R.style.LightTextViewStyle);
    }

    /**
     * Update total amount view
     */
    private void updateAmountView() {
        if (mAmount != null && mCurrency != null) {
            setText(new AmountString(mAmount, mCurrency).toString());
        }
    }
}
