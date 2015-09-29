package de.payleven.payment.example.login;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.payleven.payment.Payleven;
import de.payleven.payment.PaylevenError;
import de.payleven.payment.PaylevenFactory;
import de.payleven.payment.PaylevenRegistrationListener;
import de.payleven.payment.example.BuildConfig;
import de.payleven.payment.example.R;
import de.payleven.payment.example.SampleApplication;
import de.payleven.payment.example.view.LoadingView;

/**
 * Performs log in to payleven.
 */
public class LoginActivity extends FragmentActivity {
    //Put your api key here or updated it in the build.gradle script.
    private static final String API_KEY = BuildConfig.PAYLEVEN_API_KEY;
    private PaylevenRegistrationListenerImpl registrationListener;

    private PaylevenProvider mPaylevenProvider;

    private EditText userNameField;
    private EditText passwordField;
    private Button loginButton;
    private LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //For progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        injectPaylevenProvider();

        setUpScreenResize();
        setupViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelRegistration();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mPaylevenProvider.notifyAboutCancel();
    }

    /**
     * This method may be substituted with the real injection mechanism like Dagger.
     */
    private void injectPaylevenProvider() {
        mPaylevenProvider = ((SampleApplication) getApplication()).getPaylevenProvider();
    }

    private void setUpScreenResize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (size.y > 480) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void setupViews() {
        userNameField = (EditText) findViewById(R.id.username_field);
        userNameField.addTextChangedListener(new NonEmptyFieldWatcher());
        passwordField = (EditText) findViewById(R.id.password_field);
        passwordField.addTextChangedListener(new NonEmptyFieldWatcher());
        loginButton = (Button) findViewById(R.id.login_button);
        loadingView = (LoadingView) findViewById(R.id.loader);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                removeCursor();
                registerWithPayleven(
                        userNameField.getText().toString(),
                        passwordField.getText().toString());
            }
        });

        loadingView.setText("Logging in");
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void removeCursor() {
        userNameField.setCursorVisible(false);
        passwordField.setCursorVisible(false);
    }

    private void addCursor() {
        userNameField.setCursorVisible(true);
        passwordField.setCursorVisible(true);
    }

    private void registerWithPayleven(String userName, String password) {
        //Ignore any result from previously started registration.
        cancelRegistration();

        showProgress();
        startRegistration(userName, password);
    }

    private void cancelRegistration() {
        if (registrationListener != null) {
            registrationListener.release();
        }
    }

    /**
     * Start user registration with mPOS SDK.
     *
     * Values username and password have to be replaced by the user's payleven merchant account.
     * In order to obtain a merchant account, a registration is required in a
     * <a href="http://payleven.com">payleven country</a>. Keep in mind that you
     * or your client has to operate in one of the countries supported by payleven.
     */
    private void startRegistration(String username, String password) {
        registrationListener = new PaylevenRegistrationListenerImpl(this, username);
        PaylevenFactory.registerAsync(getApplicationContext(),
                username,
                password,
                API_KEY,
                registrationListener);
    }

    private void showProgress() {
        loadingView.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        loadingView.setVisibility(View.GONE);
    }


    private void showError(final String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }

    /**
     * Reacts on the result of registration task. Callback methods are executed on the main thread
     */
    private class PaylevenRegistrationListenerImpl implements PaylevenRegistrationListener {
        private final String username;
        private LoginActivity activity;

        private PaylevenRegistrationListenerImpl(LoginActivity activity, String username) {
            this.activity = activity;
            this.username = username;
        }

        @Override
        public void onRegistered(Payleven payleven) {
            if (activity != null) {
                PaylevenProvider provider = activity.mPaylevenProvider;
                SampleApplication app = ((SampleApplication) activity.getApplication());
                app.setPayleven(payleven);
                app.setPaymentRepository(username);
                provider.update(payleven);
                activity.setResult(RESULT_OK);
                activity.finish();
            }
        }

        @Override
        public void onError(PaylevenError error) {
            addCursor();
            if (activity != null) {
                if (error.getCode() != 0) {
                    activity.showError("Error " + error.getCode() + "\n" + error.getMessage());
                } else {
                    activity.showError(error.getMessage());
                }
                activity.hideProgress();
            }
        }

        /**
         * Must be called when activity is dismissed in order to avoid memory leaks.
         */
        protected void release() {
            activity = null;
        }
    }

    /**
     * Reacts when either username or password field is modified
     * and enables login button when both fields are not empty
     */
    private class NonEmptyFieldWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (userNameField.getText().length() != 0 && passwordField.getText().length() != 0) {
                loginButton.setEnabled(true);
            } else {
                loginButton.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
