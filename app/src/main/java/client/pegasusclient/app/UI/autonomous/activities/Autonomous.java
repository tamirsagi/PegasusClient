package client.pegasusclient.app.UI.autonomous.activities;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import client.pegasusclient.app.BL.Services.ConnectionService;
import client.pegasusclient.app.BL.common.constants.MessageKeys;
import client.pegasusclient.app.UI.Activities.R;
import client.pegasusclient.app.UI.Helper.MyAlerts;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Tamir Sagi
 *         This class controls the Pegasus Vehicle manually using Gyro and linear acceleration
 *         digital speed on Arduino(0-255)
 *         0-40 degrees Steering Angle each side
 */
public class Autonomous extends AppCompatActivity {

    public static final String TAG = Autonomous.class.getSimpleName();


    ////////////////// SERVICES \\\\\\\\\\\\\\\\\\\\\\\\
    private Intent mConnectionManagerServiceIntent;
    private ConnectionService mConnectionService;

    private boolean mIsConnectionManagerBinned;


    // UI
    private TextView mAutoDrive;
    private TextView mFindParking;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autonomous);
        initialAutoDriveButton();
        initialFindParkingButton();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsConnectionManagerBinned) {
            unbindService(BluetoothServiceConnection);
            mIsConnectionManagerBinned = false;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mIsConnectionManagerBinned) {
            createBinnedConnectionManagerService();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    private void initialAutoDriveButton(){
        mAutoDrive = (TextView)findViewById(R.id.fab_auto_drive);
        mAutoDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectionService != null) {
                    if (mConnectionService.isConnectedToRemoteDevice()) {

                    }
                }
            }
        });


    }

    private void initialFindParkingButton(){
        mFindParking = (TextView)findViewById(R.id.fab_find_parking);
        mFindParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectionService != null) {
                    if (mConnectionService.isConnectedToRemoteDevice()) {

                    }
                }
            }
        });
    }


    /**
     *            #####  Services ######
     */


    /**
     * to Bluetooth Connection Manager Service
     */
    private void createBinnedConnectionManagerService() {
        mConnectionManagerServiceIntent = new Intent(Autonomous.this, ConnectionService.class);
        bindService(mConnectionManagerServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        mIsConnectionManagerBinned = true;
    }

    /**
     * bind to service
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.MyLocalBinder connectionManager = (ConnectionService.MyLocalBinder) service;
            mConnectionService = connectionManager.gerService();
            if (mConnectionService != null) {
                if (!mConnectionService.isConnectedToRemoteDevice()) {
                    MyAlerts.showAlertDialog(getApplicationContext());
                }else{
                    try {
                        JSONObject msg = MessageKeys.getVehicleModeMessage(MessageKeys.VEHICLE_MODE_AUTONOMOUS);
                        mConnectionService.sendMessageToRemoteDevice(MessageKeys.getProtocolMessage(msg.toString()));
                    } catch (JSONException e) {
                       Log.e(TAG,e.getMessage());
                    }
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mIsConnectionManagerBinned) {
                getApplication().unbindService(BluetoothServiceConnection);
                mIsConnectionManagerBinned = false;
            }
        }
    };



}

