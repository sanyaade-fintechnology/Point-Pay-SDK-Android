package de.payleven.payment.example.view;

import android.content.Context;
import android.util.AttributeSet;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helper view to display amount entry field together with the currency.
 */
public class AmountEntryView extends AmountView {
    //Number of digits after the decimal point
    public static final int SCALE = 2;
    private static final BigDecimal mMaxAmount = new BigDecimal("20000");

    public AmountEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Updates total amount when a number is introduced or deleted
     */
    public void updateAmount(String value) {
        BigDecimal currentAmount = getAmount();
        final BigDecimal newAmount;
        if (value == null) {
            newAmount = removeLastCharacter(currentAmount);
        } else {
            newAmount = addNewCharacter(currentAmount, value);
        }
        if (newAmount.compareTo(mMaxAmount) == -1) {
            setAmount(newAmount);
        }
    }

    /**
     * Remove value from the total amount if possible
     * @param amount total amount
     * @return total amount without last number
     */
    private BigDecimal removeLastCharacter(BigDecimal amount) {
        return amount.divide(new BigDecimal(10), SCALE, BigDecimal.ROUND_DOWN);
    }

    /**
     * Add new entered value to current total amount if possible
     * @param amount current total amount
     * @param value to add
     * @return new total amount
     */
    private BigDecimal addNewCharacter(BigDecimal amount, String value) {
        BigDecimal newValue = new BigDecimal(value);
        newValue = newValue.setScale(2, RoundingMode.DOWN);
        newValue = newValue.divide(new BigDecimal(100), SCALE, BigDecimal.ROUND_DOWN);

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return amount.add(newValue);
        } else {
            for (int i = 0; i < value.length(); i++) {
                amount = amount.multiply(new BigDecimal(10));
            }
            return amount.add(newValue);
        }
    }
}
