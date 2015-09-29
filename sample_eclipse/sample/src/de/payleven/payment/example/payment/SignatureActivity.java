package de.payleven.payment.example.payment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.payleven.payment.example.R;
import de.payleven.payment.example.view.SignatureView;

/**
 * In case the payment requires a signature, it will display a signature view.
 */
public class SignatureActivity extends AppCompatActivity {
    public static final String ACTION_CONFIRM = "action_confirm";
    public static final String ACTION_FINISH = "action_signature_finish";
    public static final String AMOUNT = "amount";

    private static final int MENU_ITEM_ERASE_SIGNATURE = 100;


    private SignatureView mSignatureView;
    private Button mPayButton;
    private View mSignHint;

    private MenuItem mEraserIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        setupActionBar();
        registerFinishListener();

        mSignHint = findViewById(R.id.sign_hint);

        mSignatureView = (SignatureView) findViewById(R.id.view_signature);
        mSignatureView.setOnSignListener(new OnSignListenerImpl());

        mPayButton = (Button) findViewById(R.id.button_pay);
        mPayButton.setOnClickListener(new OnClickListenerImpl());


        String amount = getIntent().getStringExtra(AMOUNT);
        ((TextView) findViewById(R.id.text_amount)).setText(getString(R.string.amount) + " "
            + amount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterFinishListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_ERASE_SIGNATURE, 0, "Remove Signature")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mEraserIcon = menu.getItem(0);
        mEraserIcon.setIcon(R.drawable.signature_clear_icon);
        mEraserIcon.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_ERASE_SIGNATURE:
                if (mEraserIcon.isEnabled()) {
                    eraseSignature();
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    private BroadcastReceiver mFinishListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    /**
     * Registers a broadcast receiver to catch an intent that will instruct this activity to finish.
     * This is used to close the activity after a payment timeout.
     */
    private void registerFinishListener() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mFinishListener, new IntentFilter(ACTION_FINISH));
    }

    private void unregisterFinishListener() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFinishListener);
    }

    private void eraseSignature() {
        mSignatureView.clear();
        disableEraserIcon();
        disablePayButton();
        showSignHint();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.payment));
            actionBar.setElevation(3);
        }
    }

    /**
     * Add signature to cache.
     */
    private void addSignatureImageToCache(Bitmap signature) {
        ImageCache.getInstance().setImage(signature);
    }

    private void enableEraserIcon() {
        mEraserIcon.setEnabled(true);
    }

    private void disableEraserIcon() {
        mEraserIcon.setEnabled(false);
    }

    private void showSignHint(){
        mSignHint.setVisibility(View.VISIBLE);
    }

    private void hideSignHint(){
        mSignHint.setVisibility(View.INVISIBLE);
    }

    private void enablePayButton() {
        mPayButton.setEnabled(true);
    }

    private void disablePayButton() {
        mPayButton.setEnabled(false);
    }

    /**
     * Reacts when the user signs.
     */
    private class OnSignListenerImpl implements OnSignListener {

        @Override
        public void onSign() {
            enableEraserIcon();
            enablePayButton();
            hideSignHint();
        }
    }

    /**
     * Reacts when the signature is confirmed by clicking on the Pay button
     */
    private class OnClickListenerImpl implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            addSignatureImageToCache(mSignatureView.getBitmap());

            setResult(RESULT_OK, new Intent(ACTION_CONFIRM));
            finish();
        }
    }

}
