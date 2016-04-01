package de.payleven.payment.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.payleven.payment.BluetoothError;
import de.payleven.payment.PairedDevice;
import de.payleven.payment.Payleven;
import de.payleven.payment.example.login.PaylevenProvider;

/**
 * Terminal management screen.
 * A list of paired devices is displayed if Bluetooth settings are enabled.
 * A device must be selected in order to start a payment in the future.
 */
public class DeviceManagementActivity extends AppCompatActivity {
    private PaylevenProvider mPaylevenProvider;
    private DefaultDeviceProvider mDefaultDeviceProvider;

    private ListView mDeviceListView;
    private View mNoDeviceView;
    private View mDeviceListSection;
    private TextView mNoDefaultDeviceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_management);
        inject();

        setupViews();
        setupBluetoothButton();
        setUpActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();

        mPaylevenProvider.getPayleven(new PaylevenProvider.DoneCallback() {
            @Override
            public void onDone(@NonNull Payleven payleven) {
                try {
                    PairedDevice defaultDevice = mDefaultDeviceProvider.get(payleven);
                    List<PairedDevice> pairedDevices = payleven.getPairedDevices();

                    updateDeviceInfo(defaultDevice, pairedDevices);
                } catch (BluetoothError error) {
                    showNoBluetoothError();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        mNoDeviceView = findViewById(R.id.no_devices_view);
        mDeviceListSection = findViewById(R.id.device_list_section);
        mDeviceListView = (ListView) findViewById(R.id.added_devices_list);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.terminal_list_footer,
                mDeviceListView, false);
        mDeviceListView.addFooterView(footer, null, true);
        View emptyListView = findViewById(R.id.alternative_list_empty_view);
        mDeviceListView.setEmptyView(emptyListView);
        mNoDefaultDeviceView = (TextView) findViewById(R.id.no_default_device_view);
    }

    private void setupBluetoothButton(){
        View button = findViewById(R.id.button_bluetooth_settings);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_terminals);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(3);
        }
    }

    /**
     * This method may be substituted with the real injection mechanism like Dagger.
     */
    private void inject() {
        SampleApplication app = ((SampleApplication) getApplication());
        mPaylevenProvider = app.getPaylevenProvider();
        mDefaultDeviceProvider = app.getDefaultDeviceProvider();
    }

    private void updateDeviceInfo(PairedDevice defaultDevice, List<PairedDevice> pairedDevices) {
        mNoDeviceView.setVisibility(View.GONE);
        updateDeviceList(pairedDevices, defaultDevice);
        updateDefaultDevice(defaultDevice);
    }

    private void updateDefaultDevice(PairedDevice device) {
        mDefaultDeviceProvider.set(device);
        if(device != null) {
            mNoDefaultDeviceView.setVisibility(View.GONE);
        } else {
            mNoDefaultDeviceView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDeviceList(final List<PairedDevice> pairedDevices,
                                  final PairedDevice defaultDevice) {
        if (pairedDevices.isEmpty() && defaultDevice == null) {
            showNoDevicesView();
        } else {
            showAlternativeDevices(defaultDevice, pairedDevices);
        }
    }

    private void showAlternativeDevices(final PairedDevice defaultDevice,
                                        final List<PairedDevice> allPairedDevices) {
        mDeviceListSection.setVisibility(View.VISIBLE);
        initDeviceListAdapter(defaultDevice, allPairedDevices);
    }

    private void initDeviceListAdapter(PairedDevice defaultDevice,
                                       final List<PairedDevice> allPairedDevices) {
        final TerminalListAdapter listAdapter = new TerminalListAdapter(this, allPairedDevices,
                defaultDevice);
        mDeviceListView.setAdapter(listAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PairedDevice device = (PairedDevice) adapterView.getItemAtPosition(position);
                processDeviceListClick(device, allPairedDevices);
            }
        });
    }

    private void processDeviceListClick(PairedDevice device, List<PairedDevice> allPairedDevices) {
        updateDefaultDevice(device);
        updateDeviceList(allPairedDevices, device);
    }

    private void showNoBluetoothError() {
        TextView title = (TextView)mNoDeviceView.findViewById(R.id.no_device_title);
        title.setText(R.string.no_bluetooth_error);
        showNoDevicesView();
    }

    private void showNoDevicesView() {
        mDeviceListSection.setVisibility(View.GONE);
        mNoDeviceView.setVisibility(View.VISIBLE);
    }

    /**
     * Displays the list of terminals valid for the payment
     */
    private static class TerminalListAdapter extends ArrayAdapter<PairedDevice> {
        private LayoutInflater mInflater;
        private PairedDevice mDefaultDevice;

        private TerminalListAdapter(Context context, List<PairedDevice> devices,
                                    PairedDevice defaultDevice) {
            super(context, 0, devices);
            mInflater = LayoutInflater.from(context);
            mDefaultDevice = defaultDevice;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Don't forget the view holder for the real case
            View view = mInflater.inflate(R.layout.terminal_list_item, parent, false);
            ImageView terminalIcon = (ImageView) view.findViewById(R.id.terminal_icon_image);
            ImageView tickIcon = (ImageView) view.findViewById(R.id.tick_icon);
            TextView title = (TextView) view.findViewById(R.id.title);
            PairedDevice device = getItem(position);
            title.setText(device.getName());

            if (device.equals(mDefaultDevice)) {
                terminalIcon.setImageResource(R.drawable.icn_cnp_terminal);
                tickIcon.setVisibility(View.VISIBLE);
            } else {
                terminalIcon.setImageResource(R.drawable.icn_cnp_terminal_not_paired);
                tickIcon.setVisibility(View.GONE);
            }

            return view;
        }
    }
}
