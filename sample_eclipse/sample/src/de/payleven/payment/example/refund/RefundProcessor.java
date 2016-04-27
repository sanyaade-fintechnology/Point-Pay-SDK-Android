package de.payleven.payment.example.refund;

import java.math.BigDecimal;
import java.util.Currency;

import de.payleven.payment.Payleven;
import de.payleven.payment.PaylevenError;
import de.payleven.payment.RefundListener;
import de.payleven.payment.RefundRequest;
import de.payleven.payment.RefundResult;
import de.payleven.payment.RefundTask;
import de.payleven.payment.example.payment.PaymentRepository;
import de.payleven.payment.example.payment.SalePayment;

/**
 * Provides api for making refunds.
 */
public class RefundProcessor {

    private final Payleven mPayleven;
    private PaymentRepository mPaymentRepository;
    private RefundProcessingView mView;

    public RefundProcessor(Payleven payleven, PaymentRepository paymentRepository) {
        this.mPayleven = payleven;
        this.mPaymentRepository = paymentRepository;
    }

    /**
     * Starts the refund process.
     */
    public void doRefund(String paymentId, String refundId, BigDecimal amount, Currency currency) {
        try {
            RefundRequest request = new RefundRequest(refundId, paymentId, amount, currency, null);
            RefundTask refundTask = mPayleven.createRefundTask(request);

            startRefundTask(refundTask);
        } catch (IllegalArgumentException e) {
            mView.onError(e.getMessage());
        }
    }

    public void bindView(RefundProcessingView view) {
        mView = view;
    }

    public void unbindView(){
        mView = null;
    }

    private void startRefundTask(RefundTask refundTask) {
        refundTask.startAsync(new RefundListener() {
            @Override
            public void onError(PaylevenError error) {
                int errorCode = error.getCode();
                if (errorCode == 0) {
                    notifyError(error.getMessage());
                } else {
                    notifyError("Error " + errorCode + "\n" + error.getMessage());
                }
            }

            @Override
            public void onRefundComplete(RefundResult refundResult) {
                updatePaymentInRepository(refundResult);
                notifyRefundComplete(refundResult);
            }
        });
    }

    /**
     * Updates payment amount in the repository to be equal to the remaining one. Also sets the
     * state to {@link de.payleven.payment.example.payment.SalePayment.State#REFUNDED} if remaining
     * amount is zero.
     */
    private void updatePaymentInRepository(RefundResult result) {
        String paymentId = result.getPaymentIdentifier();
        BigDecimal remainingAmount = result.getRefundableAmount();

        mPaymentRepository.updateAmount(paymentId, remainingAmount);

        if(remainingAmount.equals(BigDecimal.ZERO)) {
            mPaymentRepository.updateState(paymentId, SalePayment.State.REFUNDED);
        }
    }

    private void notifyRefundComplete(RefundResult refundResult) {
        if(mView != null) {
            mView.onRefundComplete(refundResult);
        }
    }

    private void notifyError(String message) {
        if(mView != null){
            mView.onError(message);
        }
    }
}
