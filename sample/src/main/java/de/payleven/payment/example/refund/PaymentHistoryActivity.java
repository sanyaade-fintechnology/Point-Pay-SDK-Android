package de.payleven.payment.example.refund;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import de.payleven.payment.example.R;
import de.payleven.payment.example.SampleApplication;
import de.payleven.payment.example.payment.PaymentRepository;
import de.payleven.payment.example.payment.SalePayment;
import de.payleven.payment.example.view.AmountView;

/**
 * Shows the list of refundable payments for the current user/merchant.
 */
public class PaymentHistoryActivity extends AppCompatActivity {

    private static final int MENU_ITEM_MANUAL_ENTRY = 100;

    private PaymentRepository mPaymentRepository;
    private PaymentListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_history_activity);

        inject();
        setUpViews();
        setUpActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSalePayments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case MENU_ITEM_MANUAL_ENTRY:
                showRefundActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_MANUAL_ENTRY, 0, getString(R.string.manual_entry))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    private void inject() {
        SampleApplication app = ((SampleApplication) getApplication());
        mPaymentRepository = app.getPaymentRepository();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.refund_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(3);
        }
    }

    /**
     * Loads the list of {@link SalePayment} made by a merchant.
     */
    private void fetchSalePayments() {
        new AsyncTask<Void, Void, List<SalePayment>>() {
            @Override
            protected List<SalePayment> doInBackground(Void... voids) {
                return mPaymentRepository.getAllApproved();
            }

            @Override
            protected void onPostExecute(List<SalePayment> salePayments) {
                mListAdapter.clear();
                mListAdapter.addAll(salePayments);
            }
        }.execute();
    }

    private void setUpViews() {
        ListView paymentListView = (ListView) findViewById(R.id.payment_list_view);
        mListAdapter = new PaymentListAdapter(this);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.payment_list_header,
                paymentListView, false);
        paymentListView.addHeaderView(header);

        paymentListView.setAdapter(mListAdapter);
        paymentListView.setOnItemClickListener(new ListItemClickListener());
        paymentListView.setEmptyView(findViewById(R.id.no_payments_view));
    }

    private void showRefundActivity() {
        Intent intent = new Intent(this, RefundActivity.class);
        startActivity(intent);
    }

    private void showRefundActivityPopulated(String id, BigDecimal amount, Currency currency) {
        Intent intent = new Intent(this, RefundActivity.class);
        intent.putExtra(RefundActivity.PAYMENT_ID, id);
        intent.putExtra(RefundActivity.PAYMENT_AMOUNT, amount);
        intent.putExtra(RefundActivity.PAYMENT_CURRENCY, currency);
        startActivity(intent);
    }

    /**
     * Reacts when an {@link SalePayment} from the selector list is clicked and
     * creates and intent with the necessary payment data.
     */
    private class ListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            SalePayment payment = (SalePayment) adapterView.getItemAtPosition(position);
            if (payment != null) {
                showRefundActivityPopulated(payment.getId(), payment.getAmount(),
                        payment.getCurrency());
            }
        }
    }

    /**
     * Refundable payment list adapter. Shows payment date, payment amount and payment id.
     */
    private static class PaymentListAdapter extends ArrayAdapter<SalePayment> {
        private LayoutInflater mInflater;

        public PaymentListAdapter(Context context) {
            this(context, new ArrayList<SalePayment>());
        }

        public PaymentListAdapter(Context context, List<SalePayment> items) {
            super(context, 0, items);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SalePayment payment = getItem(position);
            View view = mInflater.inflate(R.layout.payment_list_item, parent, false);
            TextView dateView = (TextView) view.findViewById(R.id.date_view);
            AmountView amountView = (AmountView) view.findViewById(R.id.amount_view);
            TextView paymentIdView = (TextView) view.findViewById(R.id.payment_id_view);

            dateView.setText(getFormattedDate(payment.getCreatedAt()));
            amountView.setAmount(payment.getAmount());
            amountView.setCurrency(payment.getCurrency());
            amountView.setTextAppearance(getContext(), R.style.RegularTextViewStyle);
            paymentIdView.setText(payment.getId());

            return view;
        }

        private String getFormattedDate(Date date) {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
        }
    }
}
