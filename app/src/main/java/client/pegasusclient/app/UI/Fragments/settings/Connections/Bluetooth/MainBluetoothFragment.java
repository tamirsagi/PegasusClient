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
import client.pegasusclient.app.BL.Services.ConnectionService;
import client.pegasusclient.app.BL.General;
import client.pegasusclient.app.BL.Util.PreferencesManager;
import client.pegasusclient.app.UI.Activities.MainApp;
import client.pegasusclient.app.UI.Activities.R;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


public class MainBluetoothFragment extends Fragment {

    // Debugging
    private static final String TAG = "BluetoothActivity";
    private static final boolean debugging = true;

    private static final String KEY_REMOTE_NAME = "Remote Device Name";
    private static final String KEY_REMOTE_ADDRESS = "Remote Device Address";
    private static final String KEY_REMOTE_SIGNAL = "Remote Device Signal";
    private static final String KEY_REMOTE_UNKNOWN = "Unknown";

    private Context mContext;

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
    private ProgressBar mLoading;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean isBluetoothEnabled;
    private boolean requestDiscoverable;
    private boolean isRegisteredToBroadcastReceiver;

    private FloatingActionsMenu mActionButtonsMenu;
    private FloatingActionButton mSearchingOtherDevices;
    private FloatingActionButton mChangeBluetoothState;

    private ConnectionService mConnectionService;
    private boolean boundToConnectionService;


    public static MainBluetoothFragment newInstance() {
        MainBluetoothFragment mbf = new MainBluetoothFragment();
        return mbf;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //A retained fragment is not destroyed during a configuration change and can be reconnected to an activity after the change
        setRetainInstance(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        createBinnedConnectionManagerService();
        mContext = getContext();



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_network_connection, container, false);
        mLoading = (ProgressBar) root.findViewById(R.id.progressBar);
        mLoading.setVisibility(View.VISIBLE);
        return root;
    }

    private void prepareBluetoothSettings() {
        mBluetoothStatus = (TextView) root.findViewById(R.id.setting_bluetooth_status_context);
        // If the adapter is null, then Bluetooth is not supported
        if (mConnectionService.getAdapter() == null) {
            showAlertDialog(getResources().getString(R.string.settings_value_not_supported));
            mBluetoothStatus.setText(getResources().getString(R.string.settings_value_not_supported));
            changeLayoutChildrenState(false);
        } else {

            mActionButtonsMenu = (FloatingActionsMenu)root.findViewById(R.id.bluetooth_settings_action_buttons_menu);
            mSearchingOtherDevices = (FloatingActionButton)root.findViewById(R.id.fab_find_devices);
            mChangeBluetoothState = (FloatingActionButton)root.findViewById(R.id.fab_change_status);

            mLocalDeviceName = (TextView) root.findViewById(R.id.setting_bluetooth_local_device_name_context);
            mLocalDeviceAddress = (TextView) root.findViewById(R.id.setting_bluetooth_local_device_address_context);
            mRemoteDeviceName = (TextView) root.findViewById(R.id.setting_bluetooth_remote_device_name_context);
            mRemoteDeviceAddress = (TextView) root.findViewById(R.id.setting_bluetooth_remote_device_address_context);

            isBluetoothEnabled = mBluetoothAdapter.isEnabled();
            setDeviceState(isBluetoothEnabled);

            //If bluetooth is enabled and we are connected to the same one
            if (isBluetoothEnabled && stillConnectedToLastKnownDevice()) {
                mRemoteDeviceName.setText(PreferencesManager.getInstance(mContext).getString(KEY_REMOTE_NAME, KEY_REMOTE_UNKNOWN));
                mRemoteDeviceAddress.setText(PreferencesManager.getInstance(mContext).getString(KEY_REMOTE_ADDRESS, KEY_REMOTE_UNKNOWN));
            }


            mSearchingOtherDevices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestDiscoverable = true;
                    isBluetoothEnabled = mConnectionService.getAdapter().isEnabled();
                    if (isBluetoothEnabled) {
                        //in case the phone is not discoverable
                        if (mConnectionService.getAdapter().getScanMode() !=
                                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            ensureDiscoverable();
                        } else {
                            showOtherBluetoothDevices();
                        }
                    } else
                        ensureBluetoothEnabled();
                }
            });

            mChangeBluetoothState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeBluetoothState((isBluetoothEnabled) = mBluetoothAdapter.isEnabled());
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
        if (mConnectionService.isConnectedToRemoteDevice()) {
            BluetoothDevice remote = mConnectionService.getRemoteBluetoothDevice();
            answer = remote.getAddress().equals(PreferencesManager.getInstance(mContext).getString(KEY_REMOTE_ADDRESS, KEY_REMOTE_UNKNOWN));
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
        if (!boundToConnectionService) {
            createBinnedConnectionManagerService();             //when resume,  bind to Connection Manager Service again
            isBluetoothEnabled = mConnectionService.getAdapter().isEnabled();
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
        if (boundToConnectionService) {
            getActivity().getApplicationContext().unbindService(BluetoothServiceConnection);
            boundToConnectionService = false;
        }
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
        Intent intent = new Intent(getActivity(), ConnectionService.class);
        getActivity().getApplicationContext().bindService(intent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        boundToConnectionService = true;
    }

    /**
     * Create the service instance
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.MyLocalBinder connectionManager = (ConnectionService.MyLocalBinder) service;
            mConnectionService = connectionManager.gerService();
            prepareBluetoothSettings();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            if (boundToConnectionService) {
                getActivity().getApplicationContext().unbindService(BluetoothServiceConnection);
                boundToConnectionService = false;
            }
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
            String currentState = mConnectionService.getAdapter().EXTRA_STATE;
            int state = intent.getIntExtra(currentState, NONE);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON: {
                    showToastMessage(General.OnBluetoothIsTurningOn);
                    mBluetoothStatus.setText(getResources().getString(R.string.settings_value_disable));
                    break;
                }
                case BluetoothAdapter.STATE_ON: {
                    setDeviceState(true);
                    mLoading.setVisibility(View.GONE);
                    break;
                }
                case BluetoothAdapter.STATE_OFF:
                    mBluetoothStatus.setText(getResources().getString(R.string.settings_value_disable));
                    break;
            }
        }
    };

    /**
     * Change Bluetooth State
     */

    private void changeBluetoothState(boolean currentStatus){
        if(currentStatus){
            mBluetoothAdapter.disable();
        }else{
            mBluetoothAdapter.enable();
        }
        setDeviceState(!currentStatus);
    }


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
                // When DeviceListActivity returns with a device to connectToPegasusAP
                String btDeviceAddress = data.getExtras().getString(DevicesListDialog.EXTRA_DEVICE_ADDRESS);
                // Get the BluetoothDevice object
                BluetoothDevice remoteDevice = mConnectionService.
                        getAdapter().getRemoteDevice(btDeviceAddress);
                // Attempt to connectToPegasusAP to the device
                boolean succeed = mConnectionService.connectToRemoteDevice(remoteDevice);
                if (succeed) {
                    BluetoothDeviceInfo device = new BluetoothDeviceInfo(remoteDevice, 0);
                    mRemoteDeviceName.setText(device.getName());
                    mRemoteDeviceAddress.setText(device.getAddress());
                    PreferencesManager.getInstance(mContext).put(KEY_REMOTE_NAME, device.getName());
                    PreferencesManager.getInstance(mContext).put(KEY_REMOTE_ADDRESS, device.getAddress());
                } else
                    showAlertDialog(remoteDevice.getName());
                break;
        }
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
        mLocalDeviceName.setText(mConnectionService.getAdapter().getName());
        mLocalDeviceAddress.setText(mConnectionService.getAdapter().getAddress());
        if (isEnabled) {
            mBluetoothStatus.setText(getResources().getString(R.string.settings_value_enable));
        } else {
            mBluetoothStatus.setText(getResources().getString(R.string.settings_value_disable));
        }
    }

    /**
     * show alert message when could not connectToPegasusAP to remote device
     *
     * @param deviceName
     */
    private void showAlertDialog(String deviceName) {
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
