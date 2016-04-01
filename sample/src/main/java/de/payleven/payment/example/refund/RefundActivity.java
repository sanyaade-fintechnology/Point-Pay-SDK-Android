package de.payleven.payment.example.refund;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.example.R;
import de.payleven.payment.example.SampleApplication;
import de.payleven.payment.example.login.PaylevenProvider;

/**
 * The Refund activity handles all the refund process.
 * The UI for this process is provided by 3 fragments:
 * {@link RefundFragment} let the user introduce the data.
 * {@link RefundProcessingFragment} processes the refund data.
 * {@link RefundCompletedFragment} shows the refund result state.
 *
 * Note that the selected currency for the payment must match with the
 * configurations of the logged in merchant account.
 */
public class RefundActivity extends AppCompatActivity implements RefundInteractionListener {
    public static final String PAYMENT_ID = "payment_id";
    public static final String PAYMENT_AMOUNT = "payment_amount";
    public static final String PAYMENT_CURRENCY = "payment_currency";

    PaylevenProvider paylevenProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inject();
        setUpActionBar();

        if (savedInstanceState == null) {
            showRefundFragment();
        }
    }

    @Override
    public void processRefund(BigDecimal amount,
                              Currency currency,
                              String paymentId,
                              String refundId) {
        Fragment fragment = RefundProcessingFragment.newInstance(amount, currency, paymentId,
                refundId);
        showFragment(fragment);
    }

    @Override
    public void showRefundCompletedScreen() {
        showFragment(new RefundCompletedFragment());
    }

    @Override
    public void showRefundFragment() {
        showFragment(new RefundFragment());
    }

    private void inject(){
        SampleApplication app = (SampleApplication)getApplicationContext();
        paylevenProvider = app.getPaylevenProvider();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ft.commit();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(3);
            if (getIntent().getExtras() == null) {
                actionBar.setTitle(getResources().getString(R.string.manual_entry));
            } else {
                actionBar.setTitle("");
            }
        }
    }
}
