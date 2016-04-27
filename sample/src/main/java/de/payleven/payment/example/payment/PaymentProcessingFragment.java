package de.payleven.payment.example.payment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.Payleven;
import de.payleven.payment.PaymentProgressState;
import de.payleven.payment.PaymentResult;
import de.payleven.payment.PaymentState;
import de.payleven.payment.ReceiptConfig;
import de.payleven.payment.ReceiptGenerator;
import de.payleven.payment.example.DefaultDeviceProvider;
import de.payleven.payment.example.DeviceManagementActivity;
import de.payleven.payment.example.R;
import de.payleven.payment.example.SampleApplication;
import de.payleven.payment.example.commons.AmountString;
import de.payleven.payment.example.commons.ReceiptConfigUtil;
import de.payleven.payment.example.login.PaylevenProvider;
import de.payleven.payment.example.view.PaymentStateView;
import de.payleven.payment.tools.PaylevenTools;

/**
 * During the payment process a loading screen will be displayed, and if necessary,
 * the Signature activity will be triggered.
 * All payment logic is encapsulated in {@link PaymentProcessor} which communicates with the current
 * fragment through {@link PaymentProcessingView} interface.
 */
public class PaymentProcessingFragment extends Fragment implements PaymentProcessingView {
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_PAYMENT_ID = "payment_id";

    private static final int SIGNATURE_REQUEST_CODE = 105;

    //Are injected
    private PaymentProcessor mPaymentProcessor;
    private PaylevenProvider mPaylevenProvider;
    private DefaultDeviceProvider mDefaultDeviceProvider;
    private PaymentRepository mPaymentRepository;

    private RelativeLayout mPaymentProgressStateView;
    private View mPaymentProgressView;
    private TextView mPaymentSate;

    private FragmentInteractionListener mFragmentInteractionListener;


    public static PaymentProcessingFragment newInstance(BigDecimal amount, Currency currency,
                                                        String paymentId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_AMOUNT, amount);
        bundle.putSerializable(KEY_CURRENCY, currency);
        bundle.putString(KEY_PAYMENT_ID, paymentId);

        PaymentProcessingFragment paymentInProcessFragment = new PaymentProcessingFragment();
        paymentInProcessFragment.setArguments(bundle);

        return paymentInProcessFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_process, container, false);

        setupActionBar();
        setupPaymentProgressView(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inject();

        BigDecimal amount = (BigDecimal) getArguments().getSerializable(KEY_AMOUNT);
        Currency currency = (Currency) getArguments().getSerializable(KEY_CURRENCY);
        String paymentId = getArguments().getString(KEY_PAYMENT_ID);

        disableBackNavigation();
        startPayment(paymentId, amount, currency);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mFragmentInteractionListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentInteractionListener = null;
        mPaymentProcessor.unbindView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGNATURE_REQUEST_CODE) {
            handleSignature(resultCode, data);
        }
    }

    @Override
    public void onDefaultDeviceNotSet() {
        Intent intent = new Intent(getActivity(), DeviceManagementActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onError(String message) {
        showError(message);
        enableBackNavigation();
    }

    @Override
    public void onSignatureRequested() {
        showSignatureScreen();
    }

    @Override
    public void onPaymentComplete(PaymentResult paymentResult) {
        hideSignatureScreenIfShown();
        storeReceiptImage(paymentResult);
        showPaymentResult(paymentResult.getPaymentState());

        enableBackNavigation();
    }

    @Override
    public void onCanceledBeforePaymentStart() {
        showError(getString(R.string.payment_cancel_before_start_message));
        enableBackNavigation();
    }

    /**
     * Read payment progress state and display its respective animation/image.
     */
    @Override
    public void onPaymentProgressStateChanged(PaymentProgressState paymentProgressState) {
        Log.i("PaymentProgressState", paymentProgressState.name());
        PaylevenTools.showPaymentProgressAnimation(
                getActivity(),
                mPaymentProgressStateView,
                paymentProgressState.name());

        String paymentState = null;

        switch (paymentProgressState) {
            case STARTED:
                paymentState = getString(R.string.payment_started);
                break;
            case REQUEST_PRESENT_CARD:
                paymentState = getString(R.string.present_card);
                break;
            case REQUEST_INSERT_CARD:
                paymentState = getString(R.string.insert_card);
                break;
            case CARD_INSERTED:
                paymentState = getString(R.string.card_inserted);
                break;
            case REQUEST_ENTER_PIN:
                paymentState = getString(R.string.enter_pin);
                break;
            case PIN_ENTERED:
                paymentState = getString(R.string.pin_entered);
                break;
            case CONTACTLESS_BEEP_OK:
                paymentState = getString(R.string.beep_ok);
                break;
            case CONTACTLESS_BEEP_FAILED:
                paymentState = getString(R.string.beep_failed);
                break;
            case REQUEST_SWIPE_CARD:
                paymentState = getString(R.string.swipe_card);
                break;
            case NONE:
                paymentState = getString(R.string.none);
                break;
            default:
                break;
        }

        mPaymentSate.setText(paymentState);
    }

    private void startPayment(final String paymentId,
                              final BigDecimal amount,
                              final Currency currency) {
        PaylevenTools.showDevicePreparation(getActivity(), mPaymentProgressStateView);

        mPaylevenProvider.getPayleven(new PaylevenProvider.DoneCallback() {
            @Override
            public void onDone(@NonNull Payleven payleven) {
                mPaymentProcessor = new PaymentProcessor(payleven, mDefaultDeviceProvider,
                        mPaymentRepository);
                mPaymentProcessor.bindView(PaymentProcessingFragment.this);
                mPaymentProcessor.startPayment(paymentId, amount, currency);
            }
        });
    }

    /**
     * Handles {@link SignatureActivity} result.
     */
    private void handleSignature(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bitmap signature = ImageCache.getInstance().getAndClearImage();

            if (SignatureActivity.ACTION_CONFIRM.equals(data.getAction())) {
                mPaymentProcessor.confirmSignature(signature);
                //Signature confirmed. Wait for payment to finish
            } else {
                mPaymentProcessor.declineSignature(signature);
                //Signature declined. Wait for payment to finish
            }
        } else {
            //Payment is being cancelled. Wait for payment to finish
            //If merchant pressed Back while on the SignatureActivity,
            // the payment must be cancelled
            cancelPayment();
        }
    }

    /**
     * Will issue a broadcast request to hide the signature activity. The signature activity may be
     * still displayed in case the payment finishes because of timeout(the user doesn't take any
     * actions while on the signature screen).
     */
    private void hideSignatureScreenIfShown() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getActivity());
        bm.sendBroadcast(new Intent(SignatureActivity.ACTION_FINISH));
    }

    /**
     * Store receipt image to the temporary cache so it can be used by other fragments/activities.
     */
    private void storeReceiptImage(PaymentResult paymentResult) {
        ReceiptConfig config = ReceiptConfigUtil.prepareReceiptConfig(getActivity());

        ReceiptGenerator generator = paymentResult.getReceiptGenerator();
        Bitmap receipt = generator.generateReceipt(config);
        ImageCache.getInstance().setImage(receipt);
    }



    private void setupCancelButton(View view) {
        View mCancelButton = view.findViewById(R.id.button_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelPayment();
            }
        });
    }

    private void setupPaymentProgressView(View view) {
        mPaymentProgressView = view.findViewById(R.id.payment_progress_view);
        setupProgressView(mPaymentProgressView);
        setupCancelButton(mPaymentProgressView);
    }

    private void setupProgressView(View view) {
        mPaymentProgressStateView = (RelativeLayout)
                view.findViewById(R.id.payment_progress_state_view);
        mPaymentSate = (TextView) view.findViewById(R.id.payment_state_text);
        mPaymentSate.setText(getString(R.string.payment_in_progress));
    }

    private void enableBackNavigation() {
        mFragmentInteractionListener.setBackNavigationEnabled(true);
    }

    private void disableBackNavigation() {
        mFragmentInteractionListener.setBackNavigationEnabled(false);
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void cancelPayment() {
        mPaymentProcessor.cancel();
        mPaymentSate.setText(getString(R.string.payment_cancelling_message));
        hideCancelButton();
    }

    /**
     * This method may be substituted with the real injection mechanism like Dagger.
     */
    private void inject() {
        SampleApplication app = ((SampleApplication) getActivity().getApplication());
        mPaylevenProvider = app.getPaylevenProvider();
        mDefaultDeviceProvider = app.getDefaultDeviceProvider();
        mPaymentRepository = app.getPaymentRepository();
    }

    /**
     * Launches the signature screen. A result will be sent back once the activity is dismissed.
     * The result is handled in {@link #handleSignature(int, Intent)}
     */
    private void showSignatureScreen() {
        BigDecimal originalAmount = (BigDecimal) getArguments().getSerializable(KEY_AMOUNT);
        Currency currency = (Currency) getArguments().getSerializable(KEY_CURRENCY);

        Intent intent = new Intent(getActivity(), SignatureActivity.class);
        String amountAsString = new AmountString(originalAmount, currency).toString();
        intent.putExtra(SignatureActivity.AMOUNT, amountAsString);
        startActivityForResult(intent, SIGNATURE_REQUEST_CODE);
    }

    private void hideCancelButton() {
        getView().findViewById(R.id.button_cancel).setVisibility(View.INVISIBLE);
    }

    private void showPaymentResult(PaymentState paymentState) {
        hideProgressView();
        View completedView = getView().findViewById(R.id.completed_view);
        completedView.setVisibility(View.VISIBLE);

        setState(completedView, paymentState);
        setupDoneButton(completedView);
        setupReceiptButton(completedView);
    }

    private void setState(View view, PaymentState paymentState) {
        PaymentStateView stateView = (PaymentStateView)view.findViewById(R.id.payment_state_view);
        stateView.updateState(paymentState);
    }

    private void setupDoneButton(View view) {
        View doneButton = view.findViewById(R.id.button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentInteractionListener.showPaymentFragment();
            }
        });
    }

    private void setupReceiptButton(View view) {
        View showReceiptButton = view.findViewById(R.id.button_show_receipt);
        showReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReceipt();
            }
        });
    }

    private void showReceipt() {
        Intent intent = new Intent(getActivity(), ReceiptActivity.class);
        getActivity().startActivity(intent);
    }

    private void showError(String message) {
        hideProgressView();
        View errorView = getView().findViewById(R.id.error_view);
        errorView.setVisibility(View.VISIBLE);

        setErrorMessage(errorView, message);
        setupErrorDoneButton(errorView);
    }

    private void setErrorMessage(View view, String message) {
        TextView messageView = (TextView) view.findViewById(R.id.error_message_view);
        messageView.setText(message);
    }

    private void setupErrorDoneButton(View view) {
        view.findViewById(R.id.button_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragmentInteractionListener.showPaymentFragment();
            }
        });
    }

    private void hideProgressView() {
        mPaymentProgressView.setVisibility(View.GONE);
    }
}
