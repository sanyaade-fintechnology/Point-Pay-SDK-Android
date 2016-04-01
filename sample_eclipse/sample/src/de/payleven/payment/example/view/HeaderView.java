package de.payleven.payment.example.view;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import de.payleven.payment.example.R;

/**
 * Custom view to display the mPOS header and copyright year
 */
public class HeaderView extends LinearLayout {

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(VERTICAL);

        addTitleView(context);

        addSubtitleView(context);
    }

    private void addTitleView(Context context) {
        ImageView logo = new ImageView(context);
        logo.setImageResource(R.drawable.mpos_logo);
        MarginLayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = (int)getResources().getDimension(R.dimen.default_margin_doubled);


        addView(logo,params);
    }

    private void addSubtitleView(Context context) {
        TextView subtitle = new TextView(context);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        subtitle.setText(Html.fromHtml(context.getString(R.string.payleven) + "<sup><small>" +
                context.getString(R.string.payleven_copyright) + "</small></sup>" + year));
        subtitle.setTextAppearance(context, R.style.GreyText);
        subtitle.setGravity(Gravity.CENTER);

        addView(subtitle);
    }
}
