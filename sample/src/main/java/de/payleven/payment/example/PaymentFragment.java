package de.payleven.payment.example;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.Device;
import de.payleven.payment.DevicePreparationListener;
import de.payleven.payment.GeoLocation;
import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;
import de.payleven.payment.PaylevenError;
import de.payleven.payment.PaymentListener;
import de.payleven.payment.PaymentRequest;
import de.payleven.payment.PaymentResult;
import de.payleven.payment.PaymentTask;
import de.payleven.payment.ReceiptGenerator;
import de.payleven.payment.SignatureResponseHandler;
import de.payleven.payment.example.view.AmountEntryView;

public class PaymentFragment extends Fragment {
    //Use current location instead
    private static final GeoLocation CURRENT_LOCATION = new GeoLocation(52.5075419, 13.4261419);

    private static final int MENU_ITEM_LOGOUT = 100;
    private static final int MENU_ITEM_ADD_DEVICE = 101;

    private static final int VIEW_MODE_BASIC = 0;
    private static final int VIEW_MODE_PREPARE_PROGRESS = 1;
    private static final int VIEW_MODE_PAYMENT_PROGRESS = 2;
    private static final int VIEW_MODE_SIGNATURE_RECEIVED = 3;
    private static final int VIEW_MODE_CANCELED = 4;

    private static final int RECEIPT_WIDTH = 384;
    private static final int RECEIPT_TEXT_SIZE = 19;

    //Used to launched additional activity for signature
    private static final int SIGNATURE_REQUEST_CODE = 105;

    private int mViewMode = VIEW_MODE_BASIC;
    private FragmentInteractionListener mListener;
    private Button mPayButton;
    private Button mCancelButton;
    private TextView mStatusView;
    private ImageView mReceiptImageView;

    private SignatureResponseHandler mSignatureResponseHandler;
    private PaymentTask mPaymentTask;

    //Used to not lose the latest payment result during rotation
    private PaymentResult mLatestPaymentResult;

    private boolean isCurrentPaymentCanceled = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        View root = inflater.inflate(R.layout.fragment_payment, container, false);

        mStatusView = (TextView) root.findViewById(R.id.status_view);
        mReceiptImageView = (ImageView) root.findViewById(R.id.receipt_image_view);
        mCancelButton = (Button) root.findViewById(R.id.cancel_button);

        mPayButton = (Button) root.findViewById(R.id.pay_button);
        final AmountEntryView amountField = (AmountEntryView) root.findViewById(R.id.amount_entry_view);
        final EditText currencyField = (EditText) root.findViewById(R.id.currency_field);
        currencyField.setText("EUR");

        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BigDecimal amount = amountField.getAmount();
                    if(amount == null){
                        Toast.makeText(getActivity(), R.string.amount_null_error, Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    Currency currency = Currency.getInstance(currencyField.getText().toString());

                    //Gets payleven api or shows configuration fragment if api is null
                    Payleven api = mListener.getPaylevenApiOrConfigure();
                    //Reset the cancellation flag
                    isCurrentPaymentCanceled = false;
                    //Start device preparation which will then trigger the payment task
                    if (api != null) {
                        prepareDeviceForPayment(api, amount, currency);
                    }

                    hideKeyboard(currencyField);
                } catch (NumberFormatException e) {
                    setStatus("Invalid amount entered");
                } catch (IllegalArgumentException error) {
                    setStatus("Invalid currency");
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCurrentPaymentCanceled = true;
                setViewMode(VIEW_MODE_CANCELED);
                if (mPaymentTask != null) {
                    mPaymentTask.cancel();
                }
            }
        });

        //Restore the latest view mode
        setViewMode(mViewMode);

        //Restore the latest payment result
        if (mLatestPaymentResult != null) {
            updateLatestResult(mLatestPaymentResult);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Hide progress view in order to not intersect with other fragments when current fragment
        //is replaced.
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Do not display options if we are not in basic(non progress) mode
        if (mViewMode == VIEW_MODE_BASIC) {
            menu.add(0, MENU_ITEM_ADD_DEVICE, 0, "Devices")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(0, MENU_ITEM_LOGOUT, 0, "Logout")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_LOGOUT:
                mListener.changeConfiguration();
                return true;
            case MENU_ITEM_ADD_DEVICE:
                mListener.showDeviceManagementScreen();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGNATURE_REQUEST_CODE) {
            //When the callback is received it means we need to continue processing the payment
            if (mSignatureResponseHandler == null) {
                setStatus("Unknown state. Signature handler is null");
                return;
            }

            if (mPaymentTask == null) {
                setStatus("Unknown state. Payment task is null");
                return;
            }

            //If action was set in the Signature activity the merchant needs to
            // verify(confirm/decline) the signature
            if (resultCode == Activity.RESULT_OK) {
                setViewMode(VIEW_MODE_SIGNATURE_RECEIVED);
                Bitmap signature = SignatureCache.getInstance().getAndClearSignature();

                if (SignatureActivity.ACTION_CONFIRM.equals(data.getAction())) {
                    mSignatureResponseHandler.confirmSignature(signature);
                    setStatus("Signature confirmed. Wait for payment to finish");
                } else {
                    mSignatureResponseHandler.declineSignature(signature);
                    setStatus("Signature declined. Wait for payment to finish");
                }
            } else {
                setStatus("Payment is being cancelled. Wait for payment to finish");
                setViewMode(VIEW_MODE_CANCELED);
                //If merchant pressed Back while on the SignatureActivity, the payment must be cancelled
                mPaymentTask.cancel();

            }

            //Cleanup to avoid keeping unused SignatureResponseHandler in memory
            mSignatureResponseHandler = null;

            //Doesn't make sense to cancel the payment after the signature was processed. So
            // clean up.
            mPaymentTask = null;
        }
    }



    private void setStatus(String status) {
        mStatusView.setText(status);
    }

    private void updateLatestResult(PaymentResult paymentResult) {
        mLatestPaymentResult = paymentResult;
        String statusString = "status: " + paymentResult.getState().name();
        if (paymentResult.getSignatureImageUrl() != null) {
            statusString += "\nsignatureUrl: " + paymentResult.getSignatureImageUrl();
        }
        setStatus(statusString);

        ReceiptGenerator receiptGenerator = paymentResult.getReceiptGenerator();
        Bitmap receiptImage = receiptGenerator.generateReceipt(RECEIPT_WIDTH, RECEIPT_TEXT_SIZE);
        mReceiptImageView.setImageBitmap(receiptImage);
    }

    /**
     * Cleans up the state of the latest payment. So that already completed payment task can not
     * be referenced.
     */
    private void resetPaymentState(){
        mPaymentTask = null;
        mSignatureResponseHandler = null;
    }


    private void setViewMode(int viewMode) {
        Activity activity = getActivity();
        mViewMode = viewMode;
        if (activity != null) {
            switch (viewMode) {
                case VIEW_MODE_PREPARE_PROGRESS:
                    showPaymentProgress(activity);
                    hideCancelButton();
                    break;
                case VIEW_MODE_PAYMENT_PROGRESS:
                    showPaymentProgress(activity);
                    showCancelButton();
                    break;
                case VIEW_MODE_SIGNATURE_RECEIVED:
                    showPaymentProgress(activity);
                    hideCancelButton();
                    break;
                case VIEW_MODE_CANCELED:
                    showPaymentProgress(activity);
                    hideCancelButton();
                    break;
                default:
                    hidePaymentProgress(activity);
                    hideCancelButton();
                    break;
            }
            activity.invalidateOptionsMenu();
        }
    }

    private void showPaymentProgress(Activity activity) {
        mPayButton.setEnabled(false);
        activity.setProgressBarIndeterminateVisibility(true);
    }

    private void hidePaymentProgress(Activity activity) {
        mPayButton.setEnabled(true);
        activity.setProgressBarIndeterminateVisibility(false);
    }

    private void showCancelButton() {
        mCancelButton.setVisibility(View.VISIBLE);
    }

    private void hideCancelButton() {
        mCancelButton.setVisibility(View.INVISIBLE);
    }

    private boolean isPaymentCanceled() {
        return isCurrentPaymentCanceled;
    }

    private void prepareDeviceForPayment(@NonNull Payleven api, BigDecimal amount,
                                         Currency currency) {
        PairedDevice defaultDevice = mListener.getDefaultDevice();
        if (defaultDevice == null) {
            setStatus("Select the device in the \"Devices\" section first");
            return;
        }

        //Clean result before starting new payment
        setStatus("");
        setViewMode(VIEW_MODE_PREPARE_PROGRESS);

        api.prepareDevice(defaultDevice, new DevicePreparationListenerImpl(this, api, amount,
                currency));
    }

    private void startPayment(@NonNull Payleven api, BigDecimal amount, Currency currency,
                              Device device) {
        setViewMode(VIEW_MODE_PAYMENT_PROGRESS);
        mPaymentTask = createPaymentTask(api, amount, currency, device);
        mPaymentTask.startAsync(new PaymentListenerImpl(this));

    }

    private PaymentTask createPaymentTask(@NonNull Payleven api, BigDecimal amount,
                                          @NonNull Currency currency, Device defaultDevice) {
        String paymentId = getPaymentUniqueId();

        //Use real geo location instead of the hardcoded one
        GeoLocation currentLocation = CURRENT_LOCATION;

        PaymentRequest paymentRequest = new PaymentRequest(amount, currency,
                paymentId, currentLocation);

        return api.createPaymentTask(paymentRequest, defaultDevice);
    }

    private void showSignatureScreen(SignatureResponseHandler signatureHandler) {
        mSignatureResponseHandler = signatureHandler;

        Intent intent = new Intent(getActivity(), SignatureActivity.class);
        startActivityForResult(intent, SIGNATURE_REQUEST_CODE);
    }

    private String getPaymentUniqueId() {
        //This should be substituted with the real id from your database. This id can be used in
        // the future in order to match payleven payments to your records.
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    private static class PaymentListenerImpl implements PaymentListener {
        private final WeakReference<PaymentFragment> fragmentReference;

        private PaymentListenerImpl(PaymentFragment fragment) {
            this.fragmentReference = new WeakReference<PaymentFragment>(fragment);
        }

        @Override
        public void onPaymentComplete(PaymentResult paymentResult) {
            PaymentFragment fragment = fragmentReference.get();
            if (fragment != null && fragment.getActivity() != null) {
                fragment.setViewMode(VIEW_MODE_BASIC);

                fragment.updateLatestResult(paymentResult);
                fragment.resetPaymentState();
            }
        }

        @Override
        public void onSignatureRequested(SignatureResponseHandler signatureHandler) {
            PaymentFragment fragment = fragmentReference.get();
            if (fragment != null) {
                fragment.showSignatureScreen(signatureHandler);
            }
        }

        @Override
        public void onError(PaylevenError error) {
            PaymentFragment fragment = fragmentReference.get();
            if (fragment != null && fragment.getActivity() != null) {
                fragment.setViewMode(VIEW_MODE_BASIC);
                fragment.setStatus(error.getClass().getSimpleName() + ": " + error.getMessage());
                fragment.resetPaymentState();
            }
        }
    }

    /**
     * Prepares payleven bluetooth device and starts payment in case of success
     */
    private static class DevicePreparationListenerImpl implements DevicePreparationListener {
        //Use WeakReference in order to avoid Fragment leaking when leaving current fragment
        private WeakReference<PaymentFragment> mFragmentReference;
        private Payleven mPaylevenApi;
        private BigDecimal mAmount;
        private Currency mCurrency;

        private DevicePreparationListenerImpl(PaymentFragment fragmentReference,
                                              @NonNull Payleven api,
                                              @NonNull BigDecimal amount,
                                              @NonNull Currency currency) {
            this.mFragmentReference = new WeakReference<PaymentFragment>(fragmentReference);
            this.mPaylevenApi = api;
            this.mAmount = amount;
            this.mCurrency = currency;
        }

        @Override
        public void onDone(Device device) {
            PaymentFragment fragment = mFragmentReference.get();
            if (fragment != null) {
                if(fragment.isPaymentCanceled()){
                    fragment.setStatus("Payment canceled");
                    fragment.setViewMode(VIEW_MODE_BASIC);
                }else {
                    fragment.startPayment(mPaylevenApi, mAmount, mCurrency, device);
                }
            }
        }

        @Override
        public void onError(PaylevenError error) {
            PaymentFragment fragment = mFragmentReference.get();
            if (fragment != null && fragment.getActivity() != null) {
                fragment.setViewMode(VIEW_MODE_BASIC);
                fragment.setStatus(error.getClass().getSimpleName() + ": "
                        + error.getMessage());
            }
        }
    }
}
