package de.payleven.payment.example.login;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import de.payleven.payment.Device;
import de.payleven.payment.Payleven;
import de.payleven.payment.PaylevenError;
import de.payleven.payment.PaymentResult;
import de.payleven.payment.PaymentState;
import de.payleven.payment.PaymentTask;
import de.payleven.payment.ReceiptConfig;
import de.payleven.payment.ReceiptGenerator;
import de.payleven.payment.RefundResult;
import de.payleven.payment.RefundTask;
import de.payleven.payment.example.R;

/**
 * This stubbed {@link PaylevenProvider} demonstrates one of the possible ways to substitute the
 * real behavior of Payleven mPOS SDK with the fake one of your own. This may be useful for unit
 * testing as well as integration/UI testing.
 * NOTE: This implementation is just a demonstration. It may not always correlate with the data and
 * behavior performed by the real Payleven mPOS library.
 */
public class StubPaylevenProvider extends PaylevenProvider {
    private static Device DEFAULT_DEVICE = StubPayleven.newDevice("id", "name");
    private Payleven mPayleven;
    private Context mContext;

    private StubPaylevenProvider(Context context, Payleven payleven) {
        super(context);
        mPayleven = payleven;
        mContext = context;
    }

    @Override
    public void getPayleven(Callback callback) {
        callback.onDone(mPayleven);
    }

    public static PaylevenProvider alwaysApprovedPayleven(Context context) {
        PaymentResult result = paymentResultWithState(context, PaymentState.APPROVED);
        return new StubPaylevenProvider(context, paylevenWithPaymentResult(result));
    }

    public static PaylevenProvider alwaysCanceledPayleven(Context context) {
        PaymentResult result = paymentResultWithState(context, PaymentState.CANCELED);
        return new StubPaylevenProvider(context, paylevenWithPaymentResult(result));
    }

    public static PaylevenProvider alwaysApprovedPaylevenWithSignature(Context context) {
        PaymentResult result = paymentResultWithState(context, PaymentState.APPROVED);
        return new StubPaylevenProvider(context, paylevenWithSignature(result));
    }

    public static PaylevenProvider alwaysErrorPayleven(Context context) {
        PaylevenError error = new PaylevenError("Some error");

        return new StubPaylevenProvider(context, paylevenWithError(error));
    }

    public static PaylevenProvider alwaysApprovedRefundPayleven(Context context) {
        RefundResult refundResult = refundResult(context);
        return new StubPaylevenProvider(context, paylevenWithRefundResult(refundResult));
    }

    private static Payleven paylevenWithPaymentResult(final PaymentResult result) {
        PaymentTask task = StubPaymentTask.create(result);
        return paylevenWithDevices()
                .withPaymentTask(task)
                .build();
    }

    private static Payleven paylevenWithRefundResult(final RefundResult result) {
        RefundTask task = StubRefundTask.create(result);
        return paylevenWithDevices()
                .withRefundTask(task)
                .build();
    }

    private static Payleven paylevenWithSignature(final PaymentResult result) {
        PaymentTask task = StubPaymentTask.createWithSignature(result);
        return paylevenWithDevices()
                .withPaymentTask(task)
                .build();
    }

    private static Payleven paylevenWithError(PaylevenError error) {
        PaymentTask task = StubPaymentTask.createWithError(error);
        return paylevenWithDevices()
                .withPaymentTask(task)
                .build();
    }

    private static StubPayleven.Builder paylevenWithDevices() {
        return StubPayleven.builder()
                .withPreparedDevice(DEFAULT_DEVICE)
                .withPairedDevice(StubPayleven.randomPairedDevice())
                .withPairedDevice(StubPayleven.randomPairedDevice());
    }


    private static PaymentResult paymentResultWithState(Context context, PaymentState state) {
        return new PaymentResult(
                "id",
                state,
                BigDecimal.ONE,
                Currency.getInstance("EUR"),
                new Date(),
                null,
                null,
                receiptGenerator(context)
        );
    }

    private static RefundResult refundResult(Context context) {
        return new RefundResult(
                "refundId",
                "paymentId",
                new BigDecimal(2),
                Currency.getInstance("EUR"),
                "description",
                new Date(),
                BigDecimal.ONE,
                BigDecimal.ONE,
                "merchantid",
                receiptGenerator(context)
        );
    }

    private static ReceiptGenerator receiptGenerator(final Context context) {
        final Bitmap dummyReceipt = ((BitmapDrawable) context.getResources()
                .getDrawable(R.drawable.ic_launcher))
                .getBitmap();
        return new ReceiptGenerator() {
            @Override
            public Bitmap generateReceipt(int imageWidth, int textSize) {
                return dummyReceipt;
            }

            @Override
            public Bitmap generateReceipt(ReceiptConfig config) {
                return dummyReceipt;
            }
        };
    }


}
