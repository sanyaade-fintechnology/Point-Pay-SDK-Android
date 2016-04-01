package de.payleven.payment.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.payleven.payment.PaymentState;
import de.payleven.payment.example.R;

/**
 * Displays the state of the payment.
 */
public class PaymentStateView extends LinearLayout {
    private ImageView mStateImageView;
    private TextView mStatusView;

    public PaymentStateView(Context context) {
        super(context);
        setupViews(context);
    }

    public PaymentStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        setupViews(context);
    }

    private void setupViews(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.payment_state_view, this);

        mStateImageView = (ImageView) findViewById(R.id.state_image);
        mStatusView = (TextView) findViewById(R.id.state_message);
    }

    public void updateState(PaymentState state){
        switch (state){
            case APPROVED:
                setApprovedState();
                break;
            case CANCELED:
                setCancelledState();
                break;
            case DECLINED:
                setDeclinedState();
                break;
            default:
                throw new IllegalArgumentException("Unknown state "+state.name());
        }
    }

    private void setDeclinedState() {
        mStatusView.setText(R.string.payment_declined);
        mStateImageView.setImageResource(R.drawable.icn_failed);
        mStatusView.setTextColor(getColor(R.color.red));
    }

    private void setCancelledState() {
        mStatusView.setText(R.string.payment_cancelled);
        mStateImageView.setImageResource(R.drawable.icn_cancelled);
        mStatusView.setTextColor(getColor(R.color.dark_pink));
    }

    private void setApprovedState() {
        mStateImageView.setImageResource(R.drawable.icn_success);
        mStatusView.setText(R.string.payment_success);
        mStatusView.setTextColor(getColor(R.color.light_green));
    }

    private int getColor(int resourceId) {
        return getResources().getColor(resourceId);
    }
}
