package de.payleven.payment.example.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import de.payleven.payment.Device;
import de.payleven.payment.DevicePreparationListener;
import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;
import de.payleven.payment.PaymentRequest;
import de.payleven.payment.PaymentResult;
import de.payleven.payment.PaymentState;
import de.payleven.payment.PaymentTask;
import de.payleven.payment.RefundRequest;
import de.payleven.payment.RefundTask;

/**
 * Allows to build stubbed version of {@link Payleven}.
 */
public class StubPayleven implements Payleven {
    private final List<PairedDevice> mPairedDevices;
    private final PaymentTask mPaymentTask;
    private final Device mPreparedDevice;
    private RefundTask mRefundTask;

    public static Builder builder(){
        return new Builder();
    }

    public StubPayleven(List<PairedDevice> pairedDevices,
                        PaymentTask paymentTask,
                        RefundTask refundTask,
                        Device preparedDevice) {
        mPairedDevices = pairedDevices;
        mPaymentTask = paymentTask;
        mRefundTask = refundTask;
        mPreparedDevice = preparedDevice;
    }

    @Override
    public void prepareDevice(PairedDevice pairedDevice, DevicePreparationListener listener) {
        listener.onDone(mPreparedDevice);
    }

    @Override
    public Device prepareDeviceAndWait(PairedDevice pairedDevice) {
        return mPreparedDevice;
    }

    @Override
    public List<PairedDevice> getPairedDevices() {
        return mPairedDevices;
    }

    @Override
    public PaymentTask createPaymentTask(PaymentRequest paymentRequest, Device device) {
        return mPaymentTask;
    }

    @Override
    public RefundTask createRefundTask(RefundRequest refundRequest) {
        return mRefundTask;
    }

    @Override
    public boolean isDevicePrepared(String deviceId) {
        return mPreparedDevice != null && mPreparedDevice.getId().equals(deviceId);
    }

    public static Device newDevice(String id, String name){
        return new Device(id, name){};
    }

    public static PairedDevice newPairedDevice(String id, String name){
        return new PairedDevice(id, name){};
    }

    public static PairedDevice randomPairedDevice(){
        String id = UUID.randomUUID().toString().substring(0, 6);
        return newPairedDevice(id, "payleven-"+id);
    }

    public static class Builder{
        private List<PairedDevice> mPairedDevices = new ArrayList<PairedDevice>();
        private PaymentTask mPaymentTask;
        private RefundTask mRefundTask;
        private Device mPreparedDevice;

        private Builder(){}

        public Builder withPaymentTask(PaymentTask paymentTask){
            mPaymentTask = paymentTask;
            return this;
        }

        public Builder withRefundTask(RefundTask refundTask){
            mRefundTask = refundTask;
            return this;
        }

        public Builder withPairedDevice(PairedDevice device){
            mPairedDevices.add(device);
            return this;
        }

        public Builder withPreparedDevice(Device device){
            mPreparedDevice = device;
            return this;
        }

        public Payleven build(){
            return new StubPayleven(mPairedDevices, mPaymentTask, mRefundTask, mPreparedDevice);
        }
    }
}
