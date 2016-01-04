package client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth;


import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;
import client.pegasusclient.app.UI.Activities.R;

import java.util.Set;

/**
 * @author Tamir Sagi
 * this class is a bluetooh devices dialog. it shows paired and new devices around using bluetooth adapter
 */
public class DevicesListDialog extends DialogFragment implements BluetoothDevicesListListener{

    // Debugging
    public static final String TAG = "Devices Dialog";
    private static final boolean debugging = true;


    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    private BluetoothAdapter mBtAdapter;
    private BluetoothDevicesRecyclerAdapter mPairedDevicesRecyclerList;
    private BluetoothDevicesRecyclerAdapter mDevicesAroundRecyclerList;
    private View rootView;


    public static DevicesListDialog newInstance() {
        DevicesListDialog dialog = new DevicesListDialog();
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getResources().getString(R.string.settings_connection_title_bluetooth));
        rootView = inflater.inflate(R.layout.setting_bluetooth_devices_lists, container,
                false);

//        // Set result CANCELED in case the user backs out
//        setResult(Activity.RESULT_CANCELED);


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesRecyclerList = new BluetoothDevicesRecyclerAdapter(getContext(),R.layout.bluetooth_dialog_device_details,this);
        mDevicesAroundRecyclerList = new BluetoothDevicesRecyclerAdapter(getContext(),R.layout.bluetooth_dialog_device_details,this);

        // Find and set up the List for new devices
        ListView devicesAround = (ListView) rootView.findViewById(R.id.bluetooth_dialog_devices_around_list);
        devicesAround.setAdapter(mPairedDevicesRecyclerList);


        // Find and set up the List for paired devices
        ListView pairedDevices = (ListView) rootView.findViewById(R.id.bluetooth_dialog_paired_devices_list);
        pairedDevices.setAdapter(mDevicesAroundRecyclerList);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();


        // Get a set of currently paired devices
        Set<BluetoothDevice> bondedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the RecyclerAdapter
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                BluetoothDeviceInfo btInfo = new BluetoothDeviceInfo(device, 0);
                mPairedDevicesRecyclerList.addItem(mPairedDevicesRecyclerList.getCount(), btInfo);
            }
        }
        doDiscovery();
        rootView.setFocusable(true);
        rootView.requestFocus();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();




    }

    @Override
    public void onPause() {
        super.onPause();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        getActivity().unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (debugging)
            Log.d(TAG, "doDiscovery()");

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }


    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    //add to discovered list
                    int signal = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    mDevicesAroundRecyclerList.addItem(mDevicesAroundRecyclerList.getCount(), new BluetoothDeviceInfo(device, signal));
                }
                // When discovery is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                String noDevices = getResources().getText(R.string.none_found).toString();
//                if (mDevicesAroundRecyclerList.getCount() == 0) {
//                    mDevicesAroundRecyclerList.add(noDevices);
//                }
            }
        }
    };


    @Override
    public void getBluetoothDeviceToConnect(View v, String btDeviceAddress) {
        // Cancel discovery because it's costly and we're about to connect
        mBtAdapter.cancelDiscovery();

        // Create the result Intent and include the MAC address
        getActivity().getIntent().putExtra(EXTRA_DEVICE_ADDRESS, btDeviceAddress);
        getActivity().getIntent().putExtra("Sender", TAG);
        // Set result and finish this Activity
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
//            getActivity().setResult(Activity.RESULT_OK, intent);
        dismiss();

    }
}
