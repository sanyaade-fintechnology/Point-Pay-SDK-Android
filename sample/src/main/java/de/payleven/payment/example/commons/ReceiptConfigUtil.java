package de.payleven.payment.example.commons;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import de.payleven.payment.ReceiptConfig;
import de.payleven.payment.example.R;

/**
 * Generate the {@link de.payleven.payment.ReceiptConfig}
 */
public class ReceiptConfigUtil {

    public static final int RECEIPT_TEXT_SIZE_RATIO = 20;

    /**
     * Create a {@link ReceiptConfig} for the receipt image.
     */
    public static ReceiptConfig prepareReceiptConfig(Activity activity) {
        int width = getReceiptWidth(activity);
        int textSize = getReceiptTextSize(width);
        int lineSpacing = textSize/2;
        return new ReceiptConfig.Builder(width, textSize)
                .setLineSpacing(lineSpacing)
                .build();
    }

    /**
     * Returns an receipt width  calculated based on the current screen width.
     */
    private static int getReceiptWidth(Activity activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x - activity.getResources().getDimensionPixelSize(
                R.dimen.default_margin_doubled) * 2;
    }

    /**
     * Returns size of text on the receipt calculated based on the receipt width.
     */
    private static int getReceiptTextSize(float receiptWidth) {
        return (int) (receiptWidth / RECEIPT_TEXT_SIZE_RATIO);
    }
}
