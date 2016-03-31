package de.payleven.payment.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 *
 */
public class SignatureActivity extends Activity {
    public static final String ACTION_CONFIRM = "action_confirm";
    public static final String ACTION_DECLINE = "action_decline";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        //Setup confirm button
        findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSignatureImageToCache();

                setResult(RESULT_OK, new Intent(ACTION_CONFIRM));
                finish();
            }
        });

        //Setup decline button
        findViewById(R.id.decline_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSignatureImageToCache();

                setResult(RESULT_OK, new Intent(ACTION_DECLINE));
                finish();
            }
        });
    }

    private void addSignatureImageToCache() {
        //TODO Add real signature draw view
        ImageView imageView = (ImageView) findViewById(R.id.signature_view);
        Bitmap signature = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        //Add signature to cash and return result to the caller
        SignatureCache.getInstance().setSignature(signature);
    }
}
