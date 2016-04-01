package de.payleven.payment.example;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;

public class DeviceManagementFragment extends Fragment {
    private FragmentInteractionListener mListener;
    private ListView mAddedDevicesListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        View root = inflater.inflate(R.layout.fragment_device_manage, container, false);

        mAddedDevicesListView = (ListView) root.findViewById(R.id.added_devices_list);
        View emptyListView = root.findViewById(R.id.empty_list_view);
        mAddedDevicesListView.setEmptyView(emptyListView);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Hide progress when the view is destroyed
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        PairedDevice defaultDevice = mListener.getDefaultDevice();
        updateTerminalsList(defaultDevice);
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

    private void updateTerminalsList(final PairedDevice defaultDevice) {
        Payleven api = mListener.getPaylevenApiOrConfigure();
        if (api != null) {
            List<PairedDevice> devices = api.getPairedDevices();

            final TerminalListAdapter listAdapter = new TerminalListAdapter(getActivity(),
                    devices, defaultDevice);
            mAddedDevicesListView.setAdapter(listAdapter);
            mAddedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    PairedDevice device = (PairedDevice) adapterView.getItemAtPosition(position);
                    mListener.setDefaultDevice(device);
                    Toast.makeText(getActivity(), "Device " + device.getName() + " selected as "
                            + "default", Toast.LENGTH_SHORT).show();
                    updateTerminalsList(device);
                }
            });
        }
    }

    /**
     * Displays the list of terminals valid for the payment
     */
    private static class TerminalListAdapter extends ArrayAdapter<PairedDevice> {
        private final PairedDevice defaultDevice;

        private TerminalListAdapter(Context context, List<PairedDevice> objects,
                                    PairedDevice defaultDevice) {
            super(context, android.R.layout.simple_list_item_1, objects);
            this.defaultDevice = defaultDevice;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            //Don't forget the view holder for the real case
            TextView title = (TextView) view.findViewById(android.R.id.text1);
            PairedDevice device = getItem(position);
            title.setText(device.getName());
            if (defaultDevice != null && defaultDevice.getId().equals(device.getId())) {
                title.setTypeface(null, Typeface.BOLD);
            } else {
                title.setTypeface(null, Typeface.NORMAL);
            }

            return view;
        }
    }
}
