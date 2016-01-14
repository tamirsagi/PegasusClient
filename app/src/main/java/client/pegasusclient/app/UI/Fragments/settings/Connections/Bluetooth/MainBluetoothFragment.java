package client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;
import client.pegasusclient.app.BL.Services.ConnectionManager;
import client.pegasusclient.app.BL.Bluetooth.General;
import client.pegasusclient.app.UI.Activities.MainApp;
import client.pegasusclient.app.UI.Activities.R;


public class MainBluetoothFragment extends Fragment {

    // Debugging
    private static final String TAG = "BluetoothActivity";
    private static final boolean debugging = true;

    private static final String KEY_REMOTE_NAME = "Remote Device Name";
    private static final String KEY_REMOTE_ADDRESS = "Remote Device Address";
    private static final String KEY_REMOTE_SIGNAL = "Remote Device Signal";

    // Intent request codes
    public static final int NONE = -1;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int DISCOVERY_REQUEST = 2;
    public static final int REQUEST_CONNECT_DEVICE = 3;

    // Layout Views
    private View root;
    private TextView mBluetoothStatus;
    private TextView mLocalDeviceName;
    private TextView mLocalDeviceAddress;
    private TextView mRemoteDeviceName;
    private TextView mRemoteDeviceAddress;
    private TextView mRemoteDeviceSignal;
    private ImageButton mRefresh;
    private ImageButton mOtherDevices;
    private ImageButton mStatusButton;
    private ProgressBar mLoading;

    private boolean isBluetoothEnabled;
    private boolean requestDiscoverable;
    private boolean isRegisteredToBroadcastReceiver;

    private SharedPreferences mSharedPreferences;

    private ConnectionManager mConnectionManager;


    public static MainBluetoothFragment newInstance() {
        MainBluetoothFragment mbf = new MainBluetoothFragment();
        return mbf;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //A retained fragment is not destroyed during a configuration change and can be reconnected to an activity after the change
        setRetainInstance(true);
        createBinnedConnectionManagerService();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_network_connection, container, false);
        mSharedPreferences = getActivity().getSharedPreferences(MainApp.KEY_SHARED_PREFERNCES_NAME,Context.MODE_PRIVATE);
        mLoading = (ProgressBar) root.findViewById(R.id.progressBar);
        mLoading.setVisibility(View.VISIBLE);
        return root;
    }

    private void prepareBluetoothSettings(){
        mBluetoothStatus = (TextView) root.findViewById(R.id.setting_bluetooth_status_context);
        // If the adapter is null, then Bluetooth is not supported
        if (mConnectionManager.getAdapter() == null) {
            showAlertDialog(getResources().getString(R.string.settings_value_not_supported));
            mBluetoothStatus.setText(getResources().getString(R.string.settings_value_not_supported));
            changeLayoutChildrenState(false);
        } else {

            mRefresh = (ImageButton) root.findViewById(R.id.setting_connection_refresh_icon);
            mOtherDevices = (ImageButton) root.findViewById(R.id.setting_connection_bt_discover_icon);
            mStatusButton = (ImageButton) root.findViewById(R.id.setting_bluetooth_enable_bth);
            mLocalDeviceName = (TextView) root.findViewById(R.id.setting_bluetooth_local_device_name_context);
            mLocalDeviceAddress = (TextView) root.findViewById(R.id.setting_bluetooth_local_device_address_context);
            mRemoteDeviceName = (TextView) root.findViewById(R.id.setting_bluetooth_remote_device_name_context);
            mRemoteDeviceAddress = (TextView) root.findViewById(R.id.setting_bluetooth_remote_device_address_context);

            // If BT is not on, request that it be enabled.
            isBluetoothEnabled = mConnectionManager.getAdapter().isEnabled();
            setDeviceState(isBluetoothEnabled);

            //If bluetooth is enabled and we are connected to the same one
            if (isBluetoothEnabled && stillConnectedToLastKnownDevice()) {
                mRemoteDeviceName.setText(getStringFromSharedPreferences(KEY_REMOTE_NAME));
                mRemoteDeviceAddress.setText(getStringFromSharedPreferences(KEY_REMOTE_ADDRESS));
            }


            mOtherDevices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestDiscoverable = true;
                    isBluetoothEnabled = mConnectionManager.getAdapter().isEnabled();
                    if (isBluetoothEnabled) {
                        //in case the phone is not discoverable
                        if (mConnectionManager.getAdapter().getScanMode() !=
                                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            ensureDiscoverable();
                        } else {
                            showOtherBluetoothDevices();
                        }
                    } else
                        ensureBluetoothEnabled();
                }
            });

            mStatusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ensureBluetoothEnabled();
                }
            });


        }
        mLoading.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /**
     * check whether we are still connected to the last known device we were connected
     *
     * @return true if we are connected to the same device, no otherwise
     */
    private boolean stillConnectedToLastKnownDevice() {
        boolean answer = false;
        if (mConnectionManager.isConnectedToRemoteDevice()) {
            BluetoothDevice remote = mConnectionManager.getRemoteBluetoothDevice();
            answer = remote.getAddress().equals(getStringFromSharedPreferences(KEY_REMOTE_ADDRESS));
        }
        return answer;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (debugging) Log.e(TAG, "++ ON START ++");

    }

    @Override
    public void onResume() {
        super.onResume();
        // If BT is not on, request that it be enabled.
        if(mConnectionManager != null) {
            createBinnedConnectionManagerService();             //when resume bind to Connection Manager Service again
            isBluetoothEnabled = mConnectionManager.getAdapter().isEnabled();
            setDeviceState(isBluetoothEnabled);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRegisteredToBroadcastReceiver) {
            getActivity().unregisterReceiver(bluetoothState);   //unregister from the broadcast receiver
            isRegisteredToBroadcastReceiver = false;
        }
        if(mConnectionManager != null)
             getActivity().getApplicationContext().unbindService(BluetoothServiceConnection);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (debugging)
            Log.e(TAG, "++ ON Destroy ++");
        requestDiscoverable = false;

    }




    /**
     * Bind to Bluetooth Connection Manager Service
     */
    private void createBinnedConnectionManagerService() {
        Intent intent = new Intent(getActivity(), ConnectionManager.class);
        getActivity().getApplicationContext().bindService(intent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Create the service instance
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionManager.MyLocalBinder gpsBinder = (ConnectionManager.MyLocalBinder) service;
            mConnectionManager = gpsBinder.gerService();
            prepareBluetoothSettings();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(BluetoothServiceConnection != null)
                getActivity().getApplicationContext().unbindService(BluetoothServiceConnection);
        }
    };


    /**
     * show Toast Messages
     *
     * @param toastMsg
     */

    private void showToastMessage(String toastMsg) {
        Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_LONG).show();
    }


    /**
     * get an update when bluetooth state is changed
     */
    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String currentState = mConnectionManager.getAdapter().EXTRA_STATE;
            int state = intent.getIntExtra(currentState, NONE);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON: {
                    showToastMessage(General.OnBluetoothIsTurningOn);
                    mBluetoothStatus.setText(getResources().getString(R.string.settings_value_disable));
                    mOtherDevices.setEnabled(false);
                    break;
                }
                case BluetoothAdapter.STATE_ON: {
                    setDeviceState(true);
                    mLoading.setVisibility(View.GONE);
                    break;
                }
                case BluetoothAdapter.STATE_OFF:
                    mBluetoothStatus.setText(getResources().getString(R.string.settings_value_disable));
                    mOtherDevices.setEnabled(false);
                    break;
            }
        }
    };


    /**
     * prompt a dialog to enable bluetooth
     */
    private void ensureBluetoothEnabled() {
        mLoading.setVisibility(View.VISIBLE);
        String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
        String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        IntentFilter filter = new IntentFilter(actionStateChanged);
        getActivity().registerReceiver(bluetoothState, filter);
        isRegisteredToBroadcastReceiver = true;
        startActivityForResult(new Intent(actionRequestEnable), REQUEST_ENABLE_BT);
    }

    /**
     * Prompts a dialog to make device discoverable
     */
    private void ensureDiscoverable() {
        if (debugging)
            Log.d(TAG, "ensure discoverable");

        String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
        String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
        IntentFilter scanFilter = new IntentFilter(scanModeChanged);
        getActivity().registerReceiver(bluetoothState, scanFilter);
        startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST);
    }

    /**
     * show searching Dialog
     */
    private void showOtherBluetoothDevices() {
        DevicesListDialog dialogFrag = DevicesListDialog.newInstance();
        dialogFrag.setTargetFragment(this, REQUEST_CONNECT_DEVICE);
        dialogFrag.show(getFragmentManager(), DevicesListDialog.TAG);
        requestDiscoverable = false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    if (requestDiscoverable)
                        ensureDiscoverable();
                }
                break;
            case DISCOVERY_REQUEST:
                showOtherBluetoothDevices();
                break;
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                String btDeviceAddress = data.getExtras().getString(DevicesListDialog.EXTRA_DEVICE_ADDRESS);
                // Get the BluetoothDevice object
                BluetoothDevice remoteDevice = mConnectionManager.
                        getAdapter().getRemoteDevice(btDeviceAddress);
                // Attempt to connect to the device
                boolean succeed = mConnectionManager.connectToRemoteDevice(remoteDevice);
                if (succeed) {
                    BluetoothDeviceInfo device = new BluetoothDeviceInfo(remoteDevice, 0);
                    mRemoteDeviceName.setText(device.getName());
                    mRemoteDeviceAddress.setText(device.getAddress());
                    saveToPreferences(KEY_REMOTE_NAME,device.getName());
                    saveToPreferences(KEY_REMOTE_ADDRESS,device.getAddress());
                }
                else
                    showAlertDialog(remoteDevice.getName());
                break;
        }
    }

    /**
     * Save last device both name and address
     */
    private void saveToPreferences(String key, String value) {
        mSharedPreferences.edit().putString(key, value);
        mSharedPreferences.edit().apply();
    }


    private String getStringFromSharedPreferences(String key) {
        if(mSharedPreferences.contains(key))
            return mSharedPreferences.getString(key,getResources().getString(R.string.settings_value_unknown));

        return getResources().getString(R.string.settings_value_unknown);
    }

    /**
     * Methods changes state of children within layout
     *
     * @param state
     */
    private void changeLayoutChildrenState(boolean state) {
        LinearLayout network_connections_layout = (LinearLayout) root.findViewById(R.id.setting_network_connection);
        int size = network_connections_layout.getChildCount();
        for (int i = 0; i < size; i++) {
            View child = network_connections_layout.getChildAt(i);
            if (!(child instanceof ScrollView))
                child.setEnabled(state);
        }
    }

    /**
     * Change info regarding your device depends BT state
     *
     * @param isEnabled
     */
    private void setDeviceState(boolean isEnabled) {
        mLocalDeviceName.setText(mConnectionManager.getAdapter().getName());
        mLocalDeviceAddress.setText(mConnectionManager.getAdapter().getAddress());

        if (isEnabled) {
            mBluetoothStatus.setText(getResources().getString(R.string.settings_value_enable));
            mStatusButton.setImageResource(R.drawable.bluetooth_enable_icon);
            mStatusButton.setEnabled(false);
        } else {
            mBluetoothStatus.setText(getResources().getString(R.string.settings_value_disable));
            mStatusButton.setImageResource(R.drawable.bluetooth_disable_icon);
            mStatusButton.setEnabled(true);
        }
        mOtherDevices.setEnabled(isEnabled);
        mStatusButton.setVisibility(View.VISIBLE);
    }

    /**
     * show alert message when could not connect to remote device
     * @param deviceName
     */
    private void showAlertDialog(String deviceName){
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Dialog));
        builder.setTitle("Connection Error");
        builder.setMessage(deviceName + " Is Not Available");
        builder.setIcon(R.drawable.bluetooth_disable_icon);
        builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }


}//end of class
