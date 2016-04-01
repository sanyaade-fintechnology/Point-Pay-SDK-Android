package de.payleven.payment.example.payment;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import de.payleven.payment.example.R;

/**
 * Shows the receipt generated from a payment/refund completion result.
 */
public class ReceiptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        setUpActionBar();

        ImageView receiptView = (ImageView) findViewById(R.id.receipt_image);
        receiptView.setImageBitmap(ImageCache.getInstance().get());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.receipt);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(3);
        }
    }
}
