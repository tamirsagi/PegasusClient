package UI.UI.Activities;

import BL.Bluetooth.ConnectionManager;
import BL.Bluetooth.General;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import client.pegasusmanagement.app.R;


public class BluetoothActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "BluetoothActivity";
    private static final boolean debugging = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    public static final int NONE = -1;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int DISCOVERY_REQUEST = 2;
    public static final int REQUEST_CONNECT_DEVICE = 3;

    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (debugging)
            Log.e(TAG, "++ ON START ++");
        setContentView(R.layout.activity_bluetooth);

        // If the adapter is null, then Bluetooth is not supported
        if (ConnectionManager.getConnectionServiceInstance().getAdapter() == null) {
            Toast.makeText(this, General.OnBluetoothNotSupported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (debugging) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!ConnectionManager.getConnectionServiceInstance().getAdapter().isEnabled()) {
            //Turn on Bluetooth
            ensureBluetoothEnabled();
        }

        else if(ConnectionManager.getConnectionServiceInstance().getAdapter().getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            ensureDiscoverable();

        }
        else{
            ensureDiscoverableDevices();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (debugging)
            Log.e(TAG, "++ ON Destroy ++");

        unregisterReceiver(bluetoothState);
    }

    private void updateToastMessage(String toastMsg) {
        Toast.makeText(BluetoothActivity.this, toastMsg, Toast.LENGTH_LONG).show();
    }


    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String currentState = ConnectionManager.getConnectionServiceInstance().getAdapter().EXTRA_STATE;
            int state = intent.getIntExtra(currentState, NONE);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON: {
                    updateToastMessage(General.OnBluetoothIsTurningOn);
                    break;
                }
                case BluetoothAdapter.STATE_ON: {
                    updateToastMessage(General.OnBluetoothIsEnabled);
                    //setGUI();
                    break;
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    //register for discovery events
                    ensureDiscoverable();
                }
                break;

            case DISCOVERY_REQUEST:
                ensureDiscoverableDevices();
                break;
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DevicesListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BluetoothDevice object
                    BluetoothDevice remoteDevice = ConnectionManager.getConnectionServiceInstance().
                            getAdapter().getRemoteDevice(address);
                    // Attempt to connect to the device
                    ConnectionManager.getConnectionServiceInstance().connectToRemoteDevice(remoteDevice);
                    finish();
                }
                break;
        }
    }

    private void ensureBluetoothEnabled(){
        String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
        String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        IntentFilter filter = new IntentFilter(actionStateChanged);
        registerReceiver(bluetoothState, filter);
        startActivityForResult(new Intent(actionRequestEnable), REQUEST_ENABLE_BT);
    }

    private void ensureDiscoverable() {
        if (debugging)
            Log.d(TAG, "ensure discoverable");

        String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
        String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
        IntentFilter Scanfilter = new IntentFilter(scanModeChanged);
        registerReceiver(bluetoothState, Scanfilter);
        startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST);
    }

    private void ensureDiscoverableDevices(){
        Intent serverIntent = new Intent(this, DevicesListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }


}//end of class
