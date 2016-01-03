package client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;
import client.pegasusclient.app.UI.Activities.R;

import java.util.Set;

/**
 * @author Tamir Sagi
 */
public class DevicesListDialog extends DialogFragment {

    public static final int MAC_ADDRESS_LENGTH = 17;

    // Debugging
    private static final String TAG = "Devices_Dialog";
    private static final boolean debugging = true;


    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    private BluetoothAdapter mBtAdapter;
    private BluetoothDevicesRecyclerAdapter mPairedDevicesRecyclerList;
    private BluetoothDevicesRecyclerAdapter mDevicesAroundRecyclerList;
    private View rootView;
    private BluetoothDevicesListListener mListener;


    public static DevicesListDialog newInstance() {
        DevicesListDialog dialog = new DevicesListDialog();
//       Bundle args = new Bundle();
//       args.putInt(PAGE_NUMBER, page);
//       fragment.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.setting_bluetooth_devices_lists, container,
                false);

//        // Set result CANCELED in case the user backs out
//        setResult(Activity.RESULT_CANCELED);


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesRecyclerList = new BluetoothDevicesRecyclerAdapter(getContext());
        mDevicesAroundRecyclerList = new BluetoothDevicesRecyclerAdapter(getContext());

        // Find and set up the List for new devices
        RecyclerView devicesAround = (RecyclerView) rootView.findViewById(R.id.bluetooth_dialog_devices_around_list);
        devicesAround.setAdapter(mPairedDevicesRecyclerList);


        // Find and set up the List for paired devices
        RecyclerView pairedDevices = (RecyclerView) rootView.findViewById(R.id.bluetooth_dialog_paired_devices_list);
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

        // If there are paired devices, add each one to the ArrayAdapter
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                BluetoothDeviceInfo btInfo = new BluetoothDeviceInfo(device, 0);
                mPairedDevicesRecyclerList.addItem(mPairedDevicesRecyclerList.getItemCount(), btInfo);
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            //                if (mDevicesAroundRecyclerList.getCount() == 0) {
//                    mDevicesAroundRecyclerList.add(noDevices);
//                }

        }
        doDiscovery();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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


    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - MAC_ADDRESS_LENGTH); //remove name and set the mac only

            // Create the result Intent and include the MAC address
            getActivity().getIntent().putExtra(EXTRA_DEVICE_ADDRESS, address);
            getActivity().getIntent().putExtra("Sender", TAG);
            // Set result and finish this Activity
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
//            getActivity().setResult(Activity.RESULT_OK, intent);
            dismiss();
        }
    };

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
                    mDevicesAroundRecyclerList.addItem(mDevicesAroundRecyclerList.getItemCount(), new BluetoothDeviceInfo(device, signal));
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                String noDevices = getResources().getText(R.string.none_found).toString();
//                if (mDevicesAroundRecyclerList.getCount() == 0) {
//                    mDevicesAroundRecyclerList.add(noDevices);
//                }
            }
        }
    };


}
