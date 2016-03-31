package de.payleven.payment.example.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.example.R;

/**
 * Helper views to display amount entry field together with the currency.
 */
public class AmountEntryView extends LinearLayout {
    private EditText mAmountField;
    private TextView mCurrencyField;

    public AmountEntryView(Context context) {
        super(context);
        setupViews(context);
    }

    public AmountEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupViews(context);
    }

    public AmountEntryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupViews(context);
    }

    private void setupViews(Context context) {
        setOrientation(HORIZONTAL);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_amount, this);
        mAmountField = (EditText) findViewById(R.id.amount_entry_amount_field);
        mCurrencyField = (TextView) findViewById(R.id.amount_entry_currency_field);
    }

    public void setAmount(@NonNull BigDecimal bigDecimal) {
        mAmountField.setText(bigDecimal.toPlainString());
    }

    public void clearAmount() {
        mAmountField.setText("");
    }

    public void clearCurrency() {
        mCurrencyField.setText("");
    }

    /**
     * Returns parsed amount or null if no amount was entered.
     *
     * @throws NumberFormatException if entered string doesn't represent a decimal number
     */
    @Nullable
    public BigDecimal getAmount() throws NumberFormatException {
        String amountString = mAmountField.getText().toString();
        if(amountString == null || amountString.isEmpty()){
            return null;
        }
        return parseInputAmount(amountString);
    }

    public void setCurrency(@NonNull Currency currency) {
        mCurrencyField.setText(currency.getSymbol());
    }

    private BigDecimal parseInputAmount(@NonNull String amountString) throws NumberFormatException {
        //BigDecimal can be created only from a string with '.' as a delimiter
        amountString = amountString.replace(',', '.');
        //Check amount has not more than 2 decimal places
        int delimiterIndex = amountString.indexOf(".");
        int decimalPlaces = delimiterIndex > 0 ? amountString.length() - delimiterIndex - 1 : 0;
        if (decimalPlaces > 2) {
            throw new NumberFormatException("Number of decimal places can not be more than 2");
        }

        return new BigDecimal(amountString);
    }
}
