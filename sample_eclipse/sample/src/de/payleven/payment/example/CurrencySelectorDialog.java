package de.payleven.payment.example;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Currency;

/**
 * Provides UI for currency selection.
 */
public class CurrencySelectorDialog extends DialogFragment {
    private static final String ARG_CURRENCY = "currency";
    private CurrencySelectorListener mListener;

    public interface CurrencySelectorListener {
        void onCurrencySelected(Currency currency);
    }

    public static CurrencySelectorDialog newInstance(Currency currency){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENCY, currency);
        CurrencySelectorDialog dialog = new CurrencySelectorDialog();
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Currency currency = (Currency)getArguments().getSerializable(ARG_CURRENCY);
        View dialogView = initCurrencyDialogView(currency);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        return dialogBuilder.create();
    }

    public void setCurrencySelectorListener(CurrencySelectorListener listener) {
        this.mListener = listener;
    }

    private View initCurrencyDialogView(@Nullable Currency defaultCurrency) {
        View dialogView = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_currency_selector, null);

        if(defaultCurrency != null) {
            ((RadioButton) dialogView.findViewWithTag(defaultCurrency.toString()))
                    .setChecked(true);
        }

        ((RadioGroup) dialogView.findViewById(R.id.radiogroup_currency))
                .setOnCheckedChangeListener(new OnCurrencyCheckedChangeListener());

        return dialogView;
    }

    /**
     * Reacts when a radio button is selected in the Currency dialog
     */
    private class OnCurrencyCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            String currencyString = (String)group.findViewById(checkedId).getTag();
            notifyCurrencySelected(Currency.getInstance(currencyString));
            dismiss();
        }
    }

    private void notifyCurrencySelected(Currency currency) {
        if(mListener != null){
            mListener.onCurrencySelected(currency);
        }
    }
}
