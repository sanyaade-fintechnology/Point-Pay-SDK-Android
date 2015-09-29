package de.payleven.payment.example.refund;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.payleven.payment.example.R;
import de.payleven.payment.example.payment.ReceiptActivity;

/**
 * Fragment displayed after a refund is completed.
 * It will show the refund's state.
 */
public class RefundCompletedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_refund_completed, container, false);

        (view.findViewById(R.id.button_done)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        (view.findViewById(R.id.button_show_receipt)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReceipt();
            }
        });

        return view;
    }

    private void showReceipt() {
        Intent intent = new Intent(getActivity(), ReceiptActivity.class);
        getActivity().startActivity(intent);
    }
}
