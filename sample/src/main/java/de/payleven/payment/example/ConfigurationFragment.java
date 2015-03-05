package de.payleven.payment.example;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigurationFragment extends Fragment {
    private FragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_configure, container, false);

        final EditText userNameField = (EditText) root.findViewById(R.id.username_field);
        final EditText passwordField = (EditText) root.findViewById(R.id.password_field);
        final Button loginButton = (Button) root.findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkOrShowError(userNameField, "User name is empty")
                        && checkOrShowError(passwordField, "Password is empty")) {
                    mListener.configurePaylevenApi(
                            userNameField.getText().toString(),
                            passwordField.getText().toString());
                }
            }
        });

        return root;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Checks that the specified field contains valid data. Otherwise displays an error message
     * if specified
     *
     * @param textField    Text field to check
     * @param errorMessage Message to be displayed. If null, the message won't be displayed
     * @return true if the field contains valid data
     */
    private boolean checkOrShowError(TextView textField, String errorMessage) {
        if (textField == null) {
            throw new IllegalArgumentException("textField can not be null");
        }

        CharSequence text = textField.getText();
        if (TextUtils.isEmpty(text)) {
            if (errorMessage != null) {
                textField.setError(errorMessage);
            }
            return false;
        }

        return true;
    }
}
