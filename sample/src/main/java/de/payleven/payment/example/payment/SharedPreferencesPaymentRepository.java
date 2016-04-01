package de.payleven.payment.example.payment;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is an implementation of {@link PaymentRepository}. It uses
 * {@link android.content.SharedPreferences} to store and retrieve the user payment data.
 * In production you should use a real database.
 */
public class SharedPreferencesPaymentRepository implements PaymentRepository {

    private static final String PAYMENT_PREFS = "payment_shared_preferences";

    private final SharedPreferences mSharedPreferences;
    private final String mEmail;

    public SharedPreferencesPaymentRepository(Context context, String email) {
        mSharedPreferences = context.getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
        mEmail = email;
    }

    /**
     * Stores the merchant payment data in the shared preferences.
     * @param payment SalePayment
     */
    @Override
    public void addPayment(SalePayment payment) {
        boolean isAlreadyStored = false;
        List<SalePayment> payments= getAll();

        for (SalePayment storedPayment : payments) {
            if (storedPayment.getId().equals(payment.getId())) {
                isAlreadyStored = true;
                break;
            }
        }
        if (!isAlreadyStored) {
            payments.add(payment);
            storePayments(payments);
        }
    }

    /**
     * Returns all stored payments for the current merchant.
     */
    @Override
    public List<SalePayment> getAll() {
        if (mSharedPreferences.contains(mEmail)) {
            String jsonPaymentData = mSharedPreferences.getString(mEmail, null);
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<SalePayment>>(){}.getType();
                return gson.fromJson(jsonPaymentData, type);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<SalePayment>();
    }

    /**
     * Returns all the refundable payments from the current merchant.
     */
    @Override
    public List<SalePayment> getAllApproved() {
        List<SalePayment> payments = new ArrayList<SalePayment>();
        List<SalePayment> storedPayments = getAll();
        for (SalePayment payment : storedPayments) {
            if (payment.getState() == SalePayment.State.APPROVED &&
                    payment.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                payments.add(payment);
            }
        }
        Collections.sort(payments, new DateComparator());

        return payments;
    }

    /**
     * Updates specific payment state.
     */
    @Override
    public boolean updateState(String id, SalePayment.State state) {
        SalePayment newPayment = null, originalPayment = null;
        List<SalePayment> payments = getAll();

        for (SalePayment payment : payments) {
            if (payment.getId().equals(id)) {
                newPayment = cloneWithState(payment, state);
                originalPayment = payment;
                break;
            }
        }
        if (originalPayment != null) {
            payments.remove(originalPayment);
            payments.add(newPayment);
            storePayments(payments);

            return true;
        }
        return false;
    }

    /**
     * Updates speciffic payment amount.
     */
    @Override
    public boolean updateAmount(String id, BigDecimal amount) {
        SalePayment newPayment = null, originalPayment = null;
        List<SalePayment> payments = getAll();

        for (SalePayment payment : payments) {
            if (payment.getId().equals(id)) {
                newPayment = cloneWithAmount(payment, amount);
                originalPayment = payment;
                break;
            }
        }
        if (originalPayment != null) {
            payments.remove(originalPayment);
            payments.add(newPayment);
            storePayments(payments);

            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

    /**
     * Store payment list in shared preferences as a json string.
     */
    private void storePayments(List<SalePayment> payments) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        try {
            String jsonPaymentData = gson.toJson(payments);
            editor.putString(mEmail, jsonPaymentData);
            editor.apply();
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    private SalePayment cloneWithState(SalePayment payment, SalePayment.State state) {
        return new SalePayment(
                payment.getId(),
                payment.getCreatedAt(),
                payment.getAmount(),
                payment.getCurrency(),
                state
        );
    }

    private SalePayment cloneWithAmount(SalePayment payment, BigDecimal amount) {
        return new SalePayment(
                payment.getId(),
                payment.getCreatedAt(),
                amount,
                payment.getCurrency(),
                payment.getState()
        );
    }

    /**
     * Provide descending order of sale payments by date.
     */
    private static class DateComparator implements Comparator<SalePayment> {
        @Override
        public int compare(SalePayment payment1, SalePayment payment2) {
            //If the date is the same we check if id's are different
            int dateComparison = payment2.getCreatedAt().compareTo(payment1.getCreatedAt());
            if (dateComparison == 0) {
                return payment2.equals(payment1) ? 0 : 1;
            }

            return dateComparison;
        }
    }
}
