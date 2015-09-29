package de.payleven.payment.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.payleven.payment.example.R;

public class LoadingView extends LinearLayout {

    private TextView textview;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.view_loader, this);

        //set color for loading spinner
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.light_grey),
                        android.graphics.PorterDuff.Mode.MULTIPLY);

        textview = (TextView) view.findViewById(R.id.textview);
    }

    public void setText(String text) {
        textview.setText(text);
    }
}
