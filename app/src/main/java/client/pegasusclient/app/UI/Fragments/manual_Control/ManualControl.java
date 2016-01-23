package client.pegasusclient.app.UI.Fragments.manual_Control;

import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import client.pegasusclient.app.BL.General;
import client.pegasusclient.app.BL.Services.ConnectionService;
import client.pegasusclient.app.BL.Services.SteeringService;
import client.pegasusclient.app.UI.Activities.R;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Tamir Sagi
 *         This class controls the Pegasus Vehicle manually using Gyro and linear acceleration
 *         digital speed on Arduino(0-255)
 *         0-40 degrees Steering Angle each side
 */
public class ManualControl extends Fragment {

    public static final String TAG = "Manual Control";

    private static final double MIN_RANGE_A = 90;                              //At 90 Degree Speed gets 0 (int digital)
    private static final double MAX_RANGE_A = 74;                               //at 75 inclination speed gets 100(in digital)
    private static final double MIN_DIGITAL_SPEED_RANGE_A = 0;
    private static final double MAX_DIGITAL_SPEED_RANGE_A = 100;
    private static final double MIN_RANGE_B = 75;                            //at 75 inclination speed gets 100(in digital)
    private static final double MAX_RANGE_B = 45;                          //at 45 inclination speed gets 255(in digital)
    private static final double MIN_DIGITAL_SPEED_RANGE_B = 100;
    private static final double MAX_DIGITAL_SPEED_RANGE_B = 255;

    private static final int MAX_DIGITAL_SPEED_VALUE = 255;
    private static final int MAX_STEERING_ANGLE = 130;                      //Max Steering angle
    private static final int MIN_STEERING_ANGLE = 50;                       //Min Steering Angle
    private static final int STRAIGHT_STEERING_ANGLE = 90;
    private static final char STEERING_RIGHT = 'R';
    private static final char STEERING_LEFT = 'L';
    private static final char STEERING_NONE = 'N';

    private static final String KEY_DIGITAL_SPEED = "DS";                  //DS = Digital Speed
    private static final String KEY_ROTATION_ANGLE = "RA";                //RA = Rotation Angle
    private static final String KEY_STEERING_DIRECTION = "SD";          //Steer Direction either Right or Left

    private View root;

    private Intent mConnectionManagerServiceIntent;
    private Intent mSteeringServiceIntent;
    private ConnectionService mConnectionService;
    private SteeringService mSteeringService;

    private boolean mIsConnectionManagerBinned;
    private boolean mIsSteeringServiceBinned;

    private TextView angles;


    public static ManualControl newInstance() {
        ManualControl mc = new ManualControl();
        return mc;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBinnedConnectionManagerService();
        createBinnedSteeringService();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_manual_control, container, false);

        angles = (TextView) root.findViewById(R.id.mc_text);


        return root;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onPause() {
        super.onPause();

        if (mIsConnectionManagerBinned) {
            getActivity().getApplicationContext().unbindService(BluetoothServiceConnection);
            mIsConnectionManagerBinned = false;
        }

        if (mIsSteeringServiceBinned) {
            mSteeringService.unregisterListener();
            mSteeringService.unregisterHandler(TAG);
            getActivity().getApplicationContext().unbindService(SteeringServiceConnection);
            mIsSteeringServiceBinned = false;
            getActivity().getApplicationContext().stopService(mSteeringServiceIntent);
        }
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

        if (!mIsConnectionManagerBinned)
            createBinnedConnectionManagerService();
        if (!mIsSteeringServiceBinned)
            createBinnedSteeringService();
    }


    /**
     *            #####  Services ######
     */


    /**
     * to Bluetooth Connection Manager Service
     */
    private void createBinnedConnectionManagerService() {
        mConnectionManagerServiceIntent = new Intent(getActivity(), ConnectionService.class);
        getActivity().getApplicationContext().bindService(mConnectionManagerServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mIsConnectionManagerBinned) {
                getActivity().getApplicationContext().unbindService(BluetoothServiceConnection);
                mIsConnectionManagerBinned = false;
            }
        }
    };


    private void createBinnedSteeringService() {
        mSteeringServiceIntent = new Intent(getActivity(), SteeringService.class);
        getActivity().getApplicationContext().bindService(mSteeringServiceIntent, SteeringServiceConnection, Context.BIND_AUTO_CREATE);
        getActivity().getApplicationContext().startService(mSteeringServiceIntent);
        mIsSteeringServiceBinned = true;

    }

    private ServiceConnection SteeringServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SteeringService.MyLocalBinder steeringService = (SteeringService.MyLocalBinder) service;
            mSteeringService = steeringService.gerService();
            //register the Handler into the service to handle incoming messages
            mSteeringService.registerHandler(TAG, mMessagesHandler);
            mSteeringService.registerListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }
    };

    /**
     * handle messages to fragments
     */
    private Handler mMessagesHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SteeringService.Sender_SteeringService:
                    int inclination = msg.getData().getInt(SteeringService.KEY_INCLINATION);
                    int rotation = msg.getData().getInt(SteeringService.KEY_ROTATION);
                    angles.setText("inclination : " + inclination + "\nrotation:" + rotation);
                    int digitalSpeed = getDigitalSpeed(inclination, rotation);
                    sendDigitalSpeedToServer(digitalSpeed);
                    char steerDirection = getSteeringDirection(rotation);
                    if(steerDirection != STEERING_NONE)                       //if it's R or L
                       sendSteeringAngleToServer(steerDirection,rotation);
                    return true;
                default:
                    return false;
            }

        }
    });


    /**
     * Get Digital speed, we map range of inclination [74,90] to digital speed ->[100,0]
     * map inclination of a range[75,45] to digital speed -> [100,255]
     * using this equation :From Range[A,B] To [C,D] - > digitalSpeed = ( inclination - A)/(B-A) * (D-C) + C
     * @param inclination - phone inclination to Z axis
     * @param rotation    - phone rotation angle around XY plane;
     * @return digital speed [0,255]
     */
    private int getDigitalSpeed(int inclination, int rotation) {
        if (inclination < MAX_RANGE_B && rotation >= 0)
            return MAX_DIGITAL_SPEED_VALUE;
        else if (MAX_RANGE_A <= inclination && inclination <= MIN_RANGE_A) {  //inclination between 74-90
            return (int)((inclination - MIN_RANGE_A) / (MAX_RANGE_A - MIN_RANGE_A) *
                    (MAX_DIGITAL_SPEED_RANGE_A - MIN_DIGITAL_SPEED_RANGE_A) +
                    MIN_DIGITAL_SPEED_RANGE_A);
        } else if (MAX_RANGE_B <= inclination && inclination <= MIN_RANGE_B) {       //inclination between 75-45
            return (int)((inclination - MIN_RANGE_B) / (MAX_RANGE_B - MIN_RANGE_B) *
                    (MAX_DIGITAL_SPEED_RANGE_B - MIN_DIGITAL_SPEED_RANGE_B) +
                    MIN_DIGITAL_SPEED_RANGE_B);
        } else
            return 0;
    }


    /**
     * Function get Steering direction based on rotation angle
     * @param rotation  - phone rotation angle around XY plane;
     * @return - Direction
     */
    private char getSteeringDirection(int rotation){
        if(0 <= rotation && rotation <= STRAIGHT_STEERING_ANGLE)
            return STEERING_RIGHT;       //right when angle is 0-90 degrees
        else if (STRAIGHT_STEERING_ANGLE < rotation && rotation <= 2 * MAX_STEERING_ANGLE)
            return STEERING_LEFT;       //Left when angle is 90-180 degrees
        return STEERING_NONE;
    }

    /**
     * Method sends digital speed to server
     * @param digitalSpeed
     */
    private void sendDigitalSpeedToServer(int digitalSpeed){
        JSONObject messageToServer = new JSONObject();
        try {
            messageToServer.put(General.KEY_MESSAGE_TYPE, General.MessageType.ACTION.toString());
            messageToServer.put(General.MessageType.ACTION.toString(), General.ActionType.CHANGE_SPEED.toString());
            messageToServer.put(KEY_DIGITAL_SPEED, digitalSpeed);
            if(mConnectionService.isConnectedToRemoteDevice())
                mConnectionService.sendMessageToRemoteDevice(messageToServer.toString() + General.END_MESSAGE);
        }catch (JSONException e){

        }
    }

    /**
     * method sends steering angle to server
     * @param steerDirection - R = Right or L = Left
     * @param rotation       - Angle 0 - 40 degrees
     */
    private void sendSteeringAngleToServer(char steerDirection, double rotation){
        JSONObject messageToServer = new JSONObject();
        try {
            messageToServer.put(General.KEY_MESSAGE_TYPE, General.MessageType.ACTION.toString());
            messageToServer.put(General.MessageType.ACTION.toString(), General.ActionType.STEERING.toString());
            if(rotation < MIN_STEERING_ANGLE)
                rotation = MIN_STEERING_ANGLE;
            else if(rotation > MAX_STEERING_ANGLE)
                rotation = MAX_STEERING_ANGLE;
            messageToServer.put(KEY_STEERING_DIRECTION, steerDirection);
            // convert the rotation angle to 0-40
            if(steerDirection == STEERING_RIGHT)
                rotation = STRAIGHT_STEERING_ANGLE - rotation;
            else
                rotation -= STRAIGHT_STEERING_ANGLE;
            messageToServer.put(KEY_ROTATION_ANGLE, rotation);
            if(mConnectionService.isConnectedToRemoteDevice())
                mConnectionService.sendMessageToRemoteDevice(messageToServer.toString() + General.END_MESSAGE);
        }catch (JSONException e){

        }
    }


}

