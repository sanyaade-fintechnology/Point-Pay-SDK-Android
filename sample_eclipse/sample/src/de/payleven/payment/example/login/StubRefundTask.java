package de.payleven.payment.example.login;

import de.payleven.payment.PaylevenError;
import de.payleven.payment.RefundListener;
import de.payleven.payment.RefundResult;
import de.payleven.payment.RefundTask;

/**
 * Stub version of {@link RefundTask}.
 */
public class StubRefundTask implements RefundTask {

    private final RefundResult refundResult;
    private final PaylevenError error;

    public static StubRefundTask create(RefundResult result) {
        return new StubRefundTask(result, null);
    }

    public static StubRefundTask createWithError(PaylevenError error) {
        return new StubRefundTask(null, error);
    }

    private StubRefundTask(RefundResult refundResult, PaylevenError error) {
        this.refundResult = refundResult;
        this.error = error;
    }

    @Override
    public void startAsync(RefundListener listener) {
        if (refundResult != null) {
            listener.onRefundComplete(refundResult);
        } else if (error != null) {
            listener.onError(error);
        }
    }

    @Override
    public RefundResult startAndWait() {
        throw new IllegalStateException("Only async method is used in production for now.");
    }
}
