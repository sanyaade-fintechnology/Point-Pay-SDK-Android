package de.payleven.payment.example.payment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.example.R;

/**
 * The Payment activity handles all the payment process.
 * The UI for the payment is provided by 2 fragments:
 * {@link PaymentFragment} let the user introduce the data.
 * {@link PaymentProcessingFragment} processes the introduced data.
 *
 * Note that the selected currency for the payment must match with the
 * configurations of the logged in merchant account.
 */
public class PaymentActivity extends AppCompatActivity implements FragmentInteractionListener {
    private boolean mIsBackEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupActionBar();

        if (savedInstanceState == null) {
            showFragment(new PaymentFragment());
        }
    }

    @Override
    public void processPayment(BigDecimal amount,
                               Currency currency,
                               String externalId) {
        showFragment(PaymentProcessingFragment.newInstance(amount, currency, externalId));
    }

    @Override
    public void showPaymentFragment() {
        showFragment(new PaymentFragment());
    }

    @Override
    public void setBackNavigationEnabled(boolean enabled) {
        mIsBackEnabled = enabled;
    }

    @Override
    public void onBackPressed() {
        if(mIsBackEnabled) {
            super.onBackPressed();
        } else{
            Toast.makeText(getApplicationContext(), R.string.back_navigation_payment_message,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ft.commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.payment));
            actionBar.setElevation(3);
        }
    }
}
