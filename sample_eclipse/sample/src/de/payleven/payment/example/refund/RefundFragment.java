package de.payleven.payment.example.refund;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.example.CurrencyProvider;
import de.payleven.payment.example.CurrencySelectorDialog;
import de.payleven.payment.example.R;
import de.payleven.payment.example.SampleApplication;
import de.payleven.payment.example.payment.OnNumberPadClickListener;
import de.payleven.payment.example.view.AmountEntryView;
import de.payleven.payment.example.view.NumberPadView;
import de.payleven.payment.example.view.PaymentIdEntryView;

/**
 * Initial Refund screen, where the user selects the currency, introduces the amount
 * and the payment id of the payment he wants to refund.
 */
public class RefundFragment extends Fragment
        implements CurrencySelectorDialog.CurrencySelectorListener {

    private static final int MENU_ITEM_CHOOSE_CURRENCY = 100;

    private RefundInteractionListener mRefundInteractionListener;

    //injected
    private CurrencyProvider mCurrencyProvider;

    private Button mRefundButton;
    private AmountEntryView mAmountView;
    private PaymentIdEntryView mPaymentIdEntryView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        View root = inflater.inflate(R.layout.fragment_refund, container, false);

        setUpActionBar();

        mRefundButton = (Button) root.findViewById(R.id.pay_button);
        mRefundButton.setOnClickListener(new RefundButtonClickListener());

        mAmountView = (AmountEntryView) root.findViewById(R.id.amount_entry_view);

        mPaymentIdEntryView = (PaymentIdEntryView) root.findViewById(R.id.external_id_entry_view);
        mPaymentIdEntryView.setOnChangedView(new ExternalIdTextWatcher());
        mPaymentIdEntryView.setIcon(null, false);

        ((NumberPadView) root.findViewById(R.id.number_pad))
                .setNumberPadClickListener(new NumberPadClickListenerImpl());

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inject();
        mAmountView.setCurrency(mCurrencyProvider.getCurrency());
        handlePaymentSelection();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRefundInteractionListener = (RefundInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRefundInteractionListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity().getIntent().getExtras() == null) {
            menu.add(0, MENU_ITEM_CHOOSE_CURRENCY, 0, "Currency")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case MENU_ITEM_CHOOSE_CURRENCY:
                showCurrencySelectorDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onCurrencySelected(Currency currency) {
        mAmountView.setCurrency(currency);
        mCurrencyProvider.setCurrency(currency);
    }

    /**
     * This method may be substituted with the real injection mechanism like Dagger.
     */
    private void inject() {
        SampleApplication app = ((SampleApplication) getActivity().getApplication());
        mCurrencyProvider = app.getCurrencyProvider();
    }

    private void setUpActionBar() {
        //enable action bar back arrow
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Display dialog with 3 different currencies which the user can possibly pay with.
     */
    private void showCurrencySelectorDialog() {
        Currency latestUsedCurrency = mCurrencyProvider.getCurrency();
        CurrencySelectorDialog dialog = CurrencySelectorDialog.newInstance(latestUsedCurrency);
        dialog.setCurrencySelectorListener(this);
        dialog.show(getFragmentManager(), null);
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Reacts when a button from the number pad is clicked.
     */
    private class NumberPadClickListenerImpl implements OnNumberPadClickListener {

        @Override
        public void onPadClicked(String value) {
            mPaymentIdEntryView.clearFocus();
            hideKeyboard();
            mAmountView.updateAmount(value);
            enableOrDisablePayButton(mAmountView.getAmount(),
                    mPaymentIdEntryView.getExternalId().length());
        }
    }

    /**
     * Reacts when a new character is introduced in the external id field.
     */
    private class ExternalIdTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            enableOrDisablePayButton(mAmountView.getAmount(), s.length());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * if external id is not empty and amount != 0 then enable pay button, otherwise disable.
     *
     * @param totalAmount as double
     * @param length      of the external id
     */
    private void enableOrDisablePayButton(BigDecimal totalAmount, int length) {
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0 && length > 0) {
            mRefundButton.setEnabled(true);
        } else {
            mRefundButton.setEnabled(false);
        }
    }

    private void handlePaymentSelection() {
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            String paymentId = extras.getString(RefundActivity.PAYMENT_ID);
            Currency currency = (Currency) extras.get(RefundActivity.PAYMENT_CURRENCY);
            BigDecimal amount = (BigDecimal) extras.get(RefundActivity.PAYMENT_AMOUNT);

            mPaymentIdEntryView.setText(paymentId);
            mPaymentIdEntryView.disableExternalIdView();
            mAmountView.setAmount(amount);
            mAmountView.setCurrency(currency);
            mRefundButton.setEnabled(true);
        }
    }

    private String getRefundUniqueId() {
        //This should be substituted with the real id from your database. This id can be used in
        // the future in order to match payleven payments to your records.
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    /**
     * Start payment process when Pay button is clicked.
     */
    private class RefundButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            BigDecimal amount = mAmountView.getAmount();
            Currency currency = mAmountView.getCurrency();
            String paymentId = mPaymentIdEntryView.getExternalId();
            String refundId = getRefundUniqueId();

            mRefundInteractionListener.processRefund(amount, currency, paymentId, refundId);
        }
    }

}