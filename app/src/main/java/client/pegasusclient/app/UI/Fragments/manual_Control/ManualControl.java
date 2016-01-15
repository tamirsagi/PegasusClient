package client.pegasusclient.app.UI.Fragments.manual_Control;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import client.pegasusclient.app.BL.Services.ConnectionManager;
import client.pegasusclient.app.BL.Services.SteeringService;
import client.pegasusclient.app.UI.Activities.R;

/**
 * @author  Tamir Sagi
 * This class controls the Pegasus Vehicle using Gyro and linear acceleration
 */
public class ManualControl extends Fragment {

    private View root;
    private ConnectionManager mConnectionManager;
    private SteeringService mSteeringService;


    public static ManualControl newInstance() {
        ManualControl mc = new ManualControl();
        return mc;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBinnedConnectionManagerService();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_manual_control, container, false);





        return root;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }





    /**
     *  to Bluetooth Connection Manager Service
     */
    private void createBinnedConnectionManagerService() {
        Intent connectionManagerServiceIntent = new Intent(getActivity(), ConnectionManager.class);
        getActivity().getApplicationContext().bindService(connectionManagerServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * bind to service
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionManager.MyLocalBinder connectionManager = (ConnectionManager.MyLocalBinder) service;
            mConnectionManager = connectionManager.gerService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(mConnectionManager != null)
                getActivity().getApplicationContext().unbindService(BluetoothServiceConnection);
        }
    };

}

