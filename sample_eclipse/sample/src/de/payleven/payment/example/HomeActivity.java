package de.payleven.payment.example;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import de.payleven.payment.example.login.LogoutController;
import de.payleven.payment.example.payment.PaymentActivity;
import de.payleven.payment.example.refund.PaymentHistoryActivity;

/**
 * First screen after the login and main menu.
 */
public class HomeActivity extends FragmentActivity implements View.OnClickListener {
    LogoutController mLogoutController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        inject();

        setupButtons();

        updateVersionView();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.payment_button:
                showPaymentScreen();
                break;
            case R.id.refund_button:
                showRefundScreen();
                break;
            case R.id.terminals_button:
                showDeviceManagementScreen();
                break;
            case R.id.logout_button:
                logout();
        }
    }

    private void setupButtons() {
        findViewById(R.id.payment_button).setOnClickListener(this);
        findViewById(R.id.refund_button).setOnClickListener(this);
        findViewById(R.id.terminals_button).setOnClickListener(this);
        findViewById(R.id.logout_button).setOnClickListener(this);
    }

    private void updateVersionView() {
        TextView versionView = (TextView)findViewById(R.id.app_version_view);
        versionView.setText(getVersionNameAndBuildNumber());
    }

    private String getVersionNameAndBuildNumber(){
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return "v" + packageInfo.versionName + " build " + packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    private void showRefundScreen() {
        Intent intent = new Intent(this, PaymentHistoryActivity.class);
        startActivity(intent);
    }

    private void showDeviceManagementScreen() {
        Intent intent = new Intent(this, DeviceManagementActivity.class);
        startActivity(intent);
    }

    private void showPaymentScreen() {
        Intent intent = new Intent(this, PaymentActivity.class);
        startActivity(intent);
    }

    private void logout() {
        mLogoutController.logoutFrom(this);
    }

    private void inject() {
        SampleApplication app = ((SampleApplication) getApplication());
        mLogoutController = app.getLogoutController();
    }
}
