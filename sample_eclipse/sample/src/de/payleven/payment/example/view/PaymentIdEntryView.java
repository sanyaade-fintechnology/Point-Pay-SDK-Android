package de.payleven.payment.example.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import de.payleven.payment.example.R;

public class PaymentIdEntryView extends LinearLayout{

    public interface OnButtonClickListener{
        void onClick(PaymentIdEntryView view);
    }

    private EditText mExternalIdView;

    public PaymentIdEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpView(context, attrs);
    }

    public void setButtonClickListener(final OnButtonClickListener listener){
        findViewById(R.id.generate_id_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(PaymentIdEntryView.this);
            }
        });
    }

    public void setOnChangedView(TextWatcher textWatcher) {
        mExternalIdView.addTextChangedListener(textWatcher);
    }

    public String getExternalId() {
        return mExternalIdView.getText().toString();
    }

    public void setText(String text){
        mExternalIdView.setText(text);
        mExternalIdView.setSelection(mExternalIdView.getText().length());
    }

    public void disableExternalIdView() {
        mExternalIdView.setEnabled(false);
    }

    public void setIcon(Drawable drawable, boolean isEnabled) {
        ((ImageView) findViewById(R.id.generate_id_view)).setImageDrawable(drawable);
        findViewById(R.id.generate_id_view).setEnabled(isEnabled);
    }

    private void setUpView(Context context, AttributeSet attrs) {
        int[] set = {
                android.R.attr.text, // idx 0
                android.R.attr.hint, // idx 1
                android.R.attr.drawable  // idx 2
        };
        TypedArray a = context.obtainStyledAttributes(attrs, set);


        try {
            String text = a.getString(0);
            String hint = a.getString(1);
            Drawable icon = a.getDrawable(2);

            setOrientation(VERTICAL);
            View view = inflate(getContext(), R.layout.view_payment_id, this);


            mExternalIdView = (EditText) view.findViewById(R.id.external_id_edit_text_view);
            mExternalIdView.setText(text);
            mExternalIdView.setHint(hint);

            ImageView generateIdButton = (ImageView) view.findViewById(R.id.generate_id_view);
            generateIdButton.setImageDrawable(icon);
        } finally {
            a.recycle();
        }
    }
}
