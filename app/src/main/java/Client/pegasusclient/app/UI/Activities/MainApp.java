package client.pegasusclient.app.UI.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import client.pegasusclient.app.BL.Services.ConnectionService;
import client.pegasusclient.app.BL.Services.HotspotConnectivityService;
import client.pegasusclient.app.BL.common.constants.MessageKeys;
import client.pegasusclient.app.UI.autonomous.activities.Autonomous;
import client.pegasusclient.app.UI.manual.activities.ManualControl;
import org.json.JSONException;
import org.json.JSONObject;

public class MainApp extends AppCompatActivity {

    private static final String TAG = MainApp.class.getSimpleName();
    private static final int DEFAULT_VALUE = -1;

    private ImageButton mBthAbout;
    private ImageButton mBthSettings;
    private ImageButton mBthManualControl;
    private ImageButton mBthAutonomous;

    // Bluetooth connection Service
    public static ConnectionService CONNECTION_SERVICE;
    private boolean mHasBTServiceStarted;
    private boolean mIsConnectedToPegasus;

    //hotspot service
    private HotspotConnectivityService mHotspotConnectivityService;
    private boolean mHashHotspotServiceStarted;

    private int lastVehicleMode = DEFAULT_VALUE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        mBthAbout = (ImageButton) findViewById(R.id.bth_about);
        mBthAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonAboutClicked(v);
            }
        });
        mBthSettings = (ImageButton) findViewById(R.id.bth_settings);
        mBthSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonSettingsClicked(v);
            }
        });
        mBthManualControl = (ImageButton) findViewById(R.id.bth_manual_control);
        mBthManualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonManualClicked(v);
            }
        });
        mBthAutonomous = (ImageButton) findViewById(R.id.bth_autonomous);
        mBthAutonomous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onButtonAutonomousClicked(v);
            }
        });
        //bind to Bluetooth connection manager service
        createBinnedConnectionManagerService();

        //bind to wifi service
        createBinnedHotspotConnectivityServiceService();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        killBluetoothConnectionService();
        killHotspotConnectivityService();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CONNECTION_SERVICE == null) {
            createBinnedConnectionManagerService();
        }
        if (mHotspotConnectivityService == null)
            createBinnedHotspotConnectivityServiceService();

        mIsConnectedToPegasus = CONNECTION_SERVICE.isConnectedToRemoteDevice();
    }

    /**
     * handles about button
     *
     * @param view
     */
    public void onButtonAboutClicked(View view) {
        Intent about = new Intent(this, About.class);
        startActivity(about);
    }

    /**
     * handles Settings button
     *
     * @param view
     */
    public void onButtonSettingsClicked(View view) {
        Intent MainPegasusClientIntent = new Intent(this, PegasusSettings.class);
        startActivity(MainPegasusClientIntent);
    }

    /**
     * handles Manual Control button
     *
     * @param view
     */
    public void onButtonManualClicked(View view) {
        if (CONNECTION_SERVICE != null && (mIsConnectedToPegasus = CONNECTION_SERVICE.isConnectedToRemoteDevice())) {
          if(lastVehicleMode != MessageKeys.VEHICLE_MODE_MANUAL) {
              lastVehicleMode = MessageKeys.VEHICLE_MODE_MANUAL;
              try {
                  JSONObject msg = MessageKeys.getVehicleModeMessage(MessageKeys.VEHICLE_MODE_MANUAL);
                  CONNECTION_SERVICE.sendMessageToRemoteDevice(MessageKeys.getProtocolMessage(msg.toString()));
              } catch (JSONException e) {
                  Log.e(TAG, e.getMessage());
              }
          }
            Intent manualControl = new Intent(this, ManualControl.class);
            startActivity(manualControl);

        }else{
            Toast.makeText(getApplicationContext(), "Please Connect To Pegasus Vehicle First", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * handles Autonomous button
     *
     * @param view
     */
    public void onButtonAutonomousClicked(View view) {
        if (CONNECTION_SERVICE != null && (mIsConnectedToPegasus = CONNECTION_SERVICE.isConnectedToRemoteDevice())) {
            if(lastVehicleMode != MessageKeys.VEHICLE_MODE_AUTONOMOUS) {
                lastVehicleMode = MessageKeys.VEHICLE_MODE_AUTONOMOUS;
                try {
                    JSONObject msg = MessageKeys.getVehicleModeMessage(MessageKeys.VEHICLE_MODE_AUTONOMOUS);
                    CONNECTION_SERVICE.sendMessageToRemoteDevice(MessageKeys.getProtocolMessage(msg.toString()));
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            Intent autonomous_intent = new Intent(this, Autonomous.class);
            startActivity(autonomous_intent);
        }else{
            Toast.makeText(getApplicationContext(), "Please Connect To Pegasus Vehicle First", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Bind to Bluetooth Connection Manager Service
     */
    private void createBinnedConnectionManagerService() {
        Intent connectionManagerServiceIntent = new Intent(this, ConnectionService.class);
        bindService(connectionManagerServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        if (!mHasBTServiceStarted) {
            startService(connectionManagerServiceIntent);
            mHasBTServiceStarted = true;
        }
    }

    /**
     * Create the service instance
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.MyLocalBinder connectionManager = (ConnectionService.MyLocalBinder) service;
            CONNECTION_SERVICE = connectionManager.gerService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (BluetoothServiceConnection != null)
                unbindService(BluetoothServiceConnection);
        }
    };

    /**
     * destroy bluetooth connection service
     */
    private void killBluetoothConnectionService() {
        if (CONNECTION_SERVICE != null) {
            CONNECTION_SERVICE.stopSelf();      //kill connection service
            CONNECTION_SERVICE = null;
            mHasBTServiceStarted = false;
        }
    }


    /**
     * Bind to Hotspot Connection Manager Service
     */
    private void createBinnedHotspotConnectivityServiceService() {
        Intent intent = new Intent(this, HotspotConnectivityService.class);
        bindService(intent, hotspotConnectivityService, Context.BIND_AUTO_CREATE);
        if (!mHashHotspotServiceStarted) {
            startService(intent);
            mHasBTServiceStarted = true;
        }
    }

    /**
     * Create the service instance
     */
    private ServiceConnection hotspotConnectivityService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HotspotConnectivityService.MyLocalBinder hotspotConnectivityService = (HotspotConnectivityService.MyLocalBinder) service;
            mHotspotConnectivityService = hotspotConnectivityService.gerService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (hotspotConnectivityService != null) {
                getApplicationContext().unbindService(hotspotConnectivityService);
            }
        }
    };

    /**
     * destroy bluetooth connection service
     */
    private void killHotspotConnectivityService() {
        if (mHotspotConnectivityService != null) {
            mHotspotConnectivityService.stopSelf();      //kill connection service
            mHotspotConnectivityService = null;
            mHashHotspotServiceStarted = false;
        }
    }

}
