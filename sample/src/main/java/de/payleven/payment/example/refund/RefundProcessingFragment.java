package de.payleven.payment.example.refund;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.Payleven;
import de.payleven.payment.PaymentResult;
import de.payleven.payment.PaymentState;
import de.payleven.payment.ReceiptConfig;
import de.payleven.payment.ReceiptGenerator;
import de.payleven.payment.RefundResult;
import de.payleven.payment.example.R;
import de.payleven.payment.example.SampleApplication;
import de.payleven.payment.example.commons.ReceiptConfigUtil;
import de.payleven.payment.example.login.PaylevenProvider;
import de.payleven.payment.example.payment.FragmentInteractionListener;
import de.payleven.payment.example.payment.ImageCache;
import de.payleven.payment.example.payment.PaymentProcessor;
import de.payleven.payment.example.payment.PaymentRepository;
import de.payleven.payment.example.payment.SignatureActivity;
import de.payleven.payment.example.view.LoadingView;

/**
 * During the refund process a loading screen will be displayed.
 * All refund logic is encapsulated in {@link RefundProcessor} which communicates with the current
 * fragment through {@link RefundProcessingView} interface.
 */
public class RefundProcessingFragment extends Fragment implements RefundProcessingView {
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_PAYMENT_ID = "payment_id";
    private static final String KEY_REFUND_ID = "refund_id";

    //Are injected
    private RefundProcessor mRefundProcessor;
    private PaymentRepository mPaymentRepository;

    private LoadingView mProgressView;
    private TextView mErrorMessageView;
    private View mErrorView;

    private RefundInteractionListener mFragmentInteractionListener;
    private PaylevenProvider mPaylevenProvider;

    public static RefundProcessingFragment newInstance(BigDecimal amount,
                                                       Currency currency,
                                                       String paymentId,
                                                       String refundId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_AMOUNT, amount);
        bundle.putSerializable(KEY_CURRENCY, currency);
        bundle.putString(KEY_PAYMENT_ID, paymentId);
        bundle.putString(KEY_REFUND_ID, refundId);

        RefundProcessingFragment paymentInProcessFragment = new RefundProcessingFragment();
        paymentInProcessFragment.setArguments(bundle);

        return paymentInProcessFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_payment_process, container, false);

        setUpActionBar();

        setupErrorView(view);
        setupProgressView(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inject();

        BigDecimal amount = (BigDecimal) getArguments().getSerializable(KEY_AMOUNT);
        Currency currency = (Currency) getArguments().getSerializable(KEY_CURRENCY);
        String paymentId = getArguments().getString(KEY_PAYMENT_ID);
        String refundId = getArguments().getString(KEY_REFUND_ID);

        showProgressView();
        startRefund(amount, currency, paymentId, refundId);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mFragmentInteractionListener = (RefundInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentInteractionListener = null;
        mRefundProcessor.unbindView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            mFragmentInteractionListener.showRefundFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onError(String message) {
        showError(message);
    }

    @Override
    public void onRefundComplete(RefundResult result) {
        storeReceiptImage(result);
        mFragmentInteractionListener.showRefundCompletedScreen();
    }

    /**
     * Store receipt image to the temporary cache so it can be used by other fragments/activities.
     */
    private void storeReceiptImage(RefundResult refundResult) {
        ReceiptConfig config = ReceiptConfigUtil.prepareReceiptConfig(getActivity());

        ReceiptGenerator generator = refundResult.getReceiptGenerator();
        Bitmap receipt = generator.generateReceipt(config);
        ImageCache.getInstance().setImage(receipt);
    }

    private void setupErrorView(View view) {
        mErrorView = view.findViewById(R.id.error_view);
        mErrorMessageView = (TextView) view.findViewById(R.id.error_message_view);
        mErrorView.findViewById(R.id.button_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    private void setupProgressView(View view) {
        view.findViewById(R.id.button_cancel).setVisibility(View.INVISIBLE);
        mProgressView = (LoadingView) view.findViewById(R.id.payment_process_loader);
        mProgressView.setText(getString(R.string.refund_in_progress));
    }

    private void showProgressView() {
        mProgressView.setVisibility(View.VISIBLE);
    }

    private void hideProgressView() {
        mProgressView.setVisibility(View.GONE);
    }

    private void startRefund(final BigDecimal amount,
                             final Currency currency,
                             final String paymentId,
                             final String refundId) {
        mPaylevenProvider.getPayleven(new PaylevenProvider.DoneCallback() {
            @Override
            public void onDone(@NonNull Payleven payleven) {
                mRefundProcessor = new RefundProcessor(payleven, mPaymentRepository);
                mRefundProcessor.bindView(RefundProcessingFragment.this);
                mRefundProcessor.doRefund(paymentId, refundId, amount, currency);
            }
        });
    }

    private void setUpActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(getResources().getString(R.string.refund_title));
        }
    }

    /**
     * This method may be substituted with the real injection mechanism like Dagger.
     */
    private void inject() {
        SampleApplication app = ((SampleApplication) getActivity().getApplication());
        mPaylevenProvider = app.getPaylevenProvider();
        mPaymentRepository = app.getPaymentRepository();
    }

    private void showError(String message) {
        hideProgressView();
        mErrorView.setVisibility(View.VISIBLE);
        mErrorMessageView.setText(message);
    }
}
