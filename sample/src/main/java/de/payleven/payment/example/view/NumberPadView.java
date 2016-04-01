package de.payleven.payment.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import de.payleven.payment.example.payment.OnNumberPadClickListener;
import de.payleven.payment.example.R;

public class NumberPadView extends LinearLayout {

    private static OnNumberPadClickListener mListener;

    public NumberPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpView(context);
    }

    public void setNumberPadClickListener(OnNumberPadClickListener onNumberPadClickListener) {
        mListener = onNumberPadClickListener;
    }

    private void setUpView(Context context) {
        setOrientation(VERTICAL);

        View view = inflate(context, R.layout.view_number_pad, this);
        initButtons(view);
    }

    private void initButtons(View view) {
        (view.findViewById(R.id.button0)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button00)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button1)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button2)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button3)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button4)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button5)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button6)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button7)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button8)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button9)).setOnClickListener(new NumberPadClickListener());
        (view.findViewById(R.id.button_delete)).setOnClickListener(new NumberPadClickListener());
    }

    private static class NumberPadClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            mListener.onPadClicked((String) v.getTag());
        }
    }
}
