package de.payleven.payment.example.payment;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

/**
 * Holds date of a sale payment.
 */
public class SalePayment {
    public enum State {APPROVED, FAILED, REFUNDED}

    private final String mId;
    private final Date mCreatedAt;
    private final BigDecimal mAmount;
    private final Currency mCurrency;
    private final State mState;

    public SalePayment(@NonNull String mId,
                       @NonNull Date createdAt,
                       @NonNull BigDecimal mAmount,
                       @NonNull Currency currency,
                       @NonNull State state) {
        this.mId = mId;
        this.mCreatedAt = createdAt;
        this.mAmount = mAmount;
        this.mCurrency = currency;
        this.mState = state;
    }

    public String getId() {
        return mId;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public State getState() {
        return mState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SalePayment)) return false;

        SalePayment payment = (SalePayment) o;

        if (!mId.equals(payment.mId)) return false;
        if (!mCreatedAt.equals(payment.mCreatedAt)) return false;
        if (!mAmount.equals(payment.mAmount)) return false;
        if (!mCurrency.equals(payment.mCurrency)) return false;
        return mState == payment.mState;

    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mCreatedAt.hashCode();
        result = 31 * result + mAmount.hashCode();
        result = 31 * result + mCurrency.hashCode();
        result = 31 * result + mState.hashCode();
        return result;
    }
}
