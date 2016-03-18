package client.pegasusclient.app.UI.Fragments.manual_Control;

import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import client.pegasusclient.app.BL.General;
import client.pegasusclient.app.BL.Services.ConnectionService;
import client.pegasusclient.app.BL.Services.SteeringService;
import client.pegasusclient.app.UI.Activities.R;
import client.pegasusclient.app.UI.Helper.SpeedometerGauge;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Tamir Sagi
 *         This class controls the Pegasus Vehicle manually using Gyro and linear acceleration
 *         digital speed on Arduino(0-255)
 *         0-40 degrees Steering Angle each side
 */
public class ManualControl extends Fragment {

    public static final String TAG = ManualControl.class.getSimpleName();


    public enum DrivingDirection{
        FORWARD(0), REVERSE(1);

        private int value;
        DrivingDirection(int value){
            this.value = value;
        }

        public int getValue(){
            return value;
        }
    }
    ///////////////////////////////// Steering Consts \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    private static final double MIN_RANGE_A = 90;                              //At 90 Degree Speed gets 0 (int digital)
    private static final double MAX_RANGE_A = 74;                               //at 75 inclination speed gets 100(in digital)
    private static final double MIN_DIGITAL_SPEED_RANGE_A = 60;
    private static final double MAX_DIGITAL_SPEED_RANGE_A = 100;
    private static final double MIN_RANGE_B = 75;                            //at 75 inclination speed gets 100(in digital)
    private static final double MAX_RANGE_B = 45;                           //at 45 inclination speed gets 255(in digital)
    private static final double MIN_DIGITAL_SPEED_RANGE_B = 100;
    private static final double MAX_DIGITAL_SPEED_RANGE_B = 255;

    private static final int MAX_DIGITAL_SPEED_VALUE = 255;
    private static final int MAX_STEERING_ANGLE = 130;                      //Max Steering angle
    private static final int MIN_STEERING_ANGLE = 50;                       //Min Steering Angle
    private static final int STRAIGHT_STEERING_ANGLE = 90;

    private static final int MIN_ANGLE_DELTA_TO_SEND = 10;                 //Min angle delta to change
    private static final int MIN_DIGITAL_SPEED_DELTA_TO_SEND = 5;          //Min angle delta to change

    private static final String STEERING_RIGHT = "R";
    private static final String STEERING_LEFT = "L";
    private static final String STEERING_NONE = "N";

    private static final String KEY_DIGITAL_SPEED = "DS";                  //DS = Digital Speed
    private static final String KEY_ROTATION_ANGLE = "RA";                //RA = Rotation Angle
    private static final String KEY_STEERING_DIRECTION = "SD";          //Steer Direction either Right or Left

    private static final String KEY_DRIVING_DIRECTION = "DD";          //Steer Direction either Right or Left

    ///////////////////////////////// Speedometer Consts \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    private static final double MIN_DIGITAL_SPEED_GREEN_COLOR_RANGE = 55;
    private static final double MAX_DIGITAL_SPEED_GREEN_COLOR_RANGE = 150;
    private static final double MIN_DIGITAL_SPEED_YELLOW_COLOR_RANGE = 150;
    private static final double MAX_DIGITAL_SPEED_YELLOW_COLOR_RANGE = 220;
    private static final double MIN_DIGITAL_SPEED_RED_COLOR_RANGE = 220;
    private static final double MAX_DIGITAL_SPEED_RED_COLOR_RANGE = 255;

    private final String degreeSymbol  = "\u00b0";


    private View mRoot;
    private RadioGroup mDrivingDirectionRadioGroup;

    ////////////////// SERVICES \\\\\\\\\\\\\\\\\\\\\\\\
    private Intent mConnectionManagerServiceIntent;
    private Intent mSteeringServiceIntent;
    private ConnectionService mConnectionService;
    private SteeringService mSteeringService;

    private boolean mIsConnectionManagerBinned;
    private boolean mIsSteeringServiceBinned;


    private TextView mSpeed;
    private TextView mRotation;
    private TextView mDrivingDirection;
    private TextView mTotalDistance;

    private int mLastDigitalSpeed = 0;
    private int mLastSteeringAngle = 0;
    private DrivingDirection mLastDrivingDirection;
    private boolean mDirectionSet;

    private SpeedometerGauge mSpeedometerGauge;

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
        mRoot = inflater.inflate(R.layout.fragment_manual_control, container, false);
        Typeface mLedFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/digital-7/digital-7_mono.ttf");

        mSpeed = (TextView) mRoot.findViewById(R.id.manual_control_speed);
        mSpeed.setTypeface(mLedFont);

        mRotation = (TextView) mRoot.findViewById(R.id.manual_control_rotation);
        mRotation.setTypeface(mLedFont);

        mTotalDistance = (TextView) mRoot.findViewById(R.id.manual_control_distance);
        mTotalDistance.setTypeface(mLedFont);

        mDrivingDirection = (TextView) mRoot.findViewById(R.id.manual_control_driving_direction) ;
        mDrivingDirection.setTypeface(mLedFont);

        setRadioGroup();
        SetSpeedometer();
        return mRoot;
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
     * set Radio Button Group
     */
    private void setRadioGroup(){
        mDrivingDirectionRadioGroup = (RadioGroup) mRoot.findViewById(R.id.manual_control_driving_direction_radio_group);
        mDrivingDirectionRadioGroup.clearCheck();
        mDrivingDirectionRadioGroup.check(R.id.manual_control_direction_forward);       //check Forward as Default
        mDirectionSet = true;
        mDrivingDirectionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mDirectionSet = false;
                mSpeedometerGauge.clearColoredRanges();
                mSpeedometerGauge.setSpeed(0);
                switch(checkedId){
                    case R.id.manual_control_direction_forward:
                        mLastDrivingDirection = DrivingDirection.FORWARD;
                        mDrivingDirection.setText("D");
                        break;

                    case R.id.manual_control_direction_reverse:
                        mLastDrivingDirection = DrivingDirection.REVERSE;
                        mDrivingDirection.setText("R");
                        break;
                }

                sendDrivingDirection(mLastDrivingDirection);
                mDirectionSet = true;
            }
        });
    }

    /**
     * Settings for speedometer gauge
     */
    private void SetSpeedometer(){
        mSpeedometerGauge = (SpeedometerGauge) mRoot.findViewById(R.id.speedometer);
        mSpeedometerGauge.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
    }
});
        // configure value range and ticks
        mSpeedometerGauge.setMaxSpeed(MAX_DIGITAL_SPEED_VALUE);
        mSpeedometerGauge.setMajorTickStep(30);
        mSpeedometerGauge.setMinorTicks(2);
        mSpeedometerGauge.setSpeed(0);
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
                    if(mDirectionSet)
                        handleSteeringChanges(rotation,inclination);
                    return true;
                default:
                    return false;
            }

        }
    });





    /**
     * Method handles inclination and rotation.
     * @param rotation
     * @param inclination
     */
    private void handleSteeringChanges(int rotation, int inclination){
        int digitalSpeed = getDigitalSpeed(inclination, rotation);
        mSpeedometerGauge.clearColoredRanges();
        mSpeedometerGauge.setSpeed(mLastDigitalSpeed);
        mSpeed.setText("" + digitalSpeed);
        mRotation.setText("" + rotation + degreeSymbol);
        if(MIN_DIGITAL_SPEED_GREEN_COLOR_RANGE <= mLastDigitalSpeed && mLastDigitalSpeed <= MAX_DIGITAL_SPEED_GREEN_COLOR_RANGE)
            mSpeedometerGauge.addColoredRange(MIN_DIGITAL_SPEED_GREEN_COLOR_RANGE, mLastDigitalSpeed, Color.GREEN);
        else if(MIN_DIGITAL_SPEED_YELLOW_COLOR_RANGE <= mLastDigitalSpeed && mLastDigitalSpeed <= MAX_DIGITAL_SPEED_YELLOW_COLOR_RANGE){
            mSpeedometerGauge.addColoredRange(MIN_DIGITAL_SPEED_GREEN_COLOR_RANGE, MAX_DIGITAL_SPEED_GREEN_COLOR_RANGE, Color.GREEN);
            mSpeedometerGauge.addColoredRange(MIN_DIGITAL_SPEED_YELLOW_COLOR_RANGE, mLastDigitalSpeed, Color.YELLOW);
        }
        else if(MIN_DIGITAL_SPEED_RED_COLOR_RANGE <= mLastDigitalSpeed && mLastDigitalSpeed <= MAX_DIGITAL_SPEED_RED_COLOR_RANGE){
            mSpeedometerGauge.addColoredRange(MIN_DIGITAL_SPEED_GREEN_COLOR_RANGE, MAX_DIGITAL_SPEED_GREEN_COLOR_RANGE, Color.GREEN);
            mSpeedometerGauge.addColoredRange(MIN_DIGITAL_SPEED_YELLOW_COLOR_RANGE, MAX_DIGITAL_SPEED_YELLOW_COLOR_RANGE, Color.YELLOW);
            mSpeedometerGauge.addColoredRange(MIN_DIGITAL_SPEED_RED_COLOR_RANGE, mLastDigitalSpeed, Color.RED);
        }

        // Check if needs to be sent over Server
        if(Math.abs(mLastDigitalSpeed - digitalSpeed) >= MIN_DIGITAL_SPEED_DELTA_TO_SEND
                && digitalSpeed != mLastDigitalSpeed) {
            mLastDigitalSpeed = digitalSpeed;
            sendDigitalSpeedToServer(digitalSpeed);
        }
        if(Math.abs(mLastSteeringAngle - rotation) >= MIN_ANGLE_DELTA_TO_SEND
                && rotation != mLastSteeringAngle){
            mLastSteeringAngle = rotation;
            String steerDirection = getSteeringDirection(mLastSteeringAngle);
            if(!steerDirection.equals(STEERING_NONE))     //if it's R or L
                sendSteeringAngleToServer(steerDirection, mLastSteeringAngle);
        }





    }

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
     * method sends driving direction when its changed
     * @param drivingDirection  - drivign direction (FORWARD , REVERSE);
     */
    private void sendDrivingDirection(DrivingDirection drivingDirection){

        JSONObject messageToServer = new JSONObject();
        try {
            messageToServer.put(General.KEY_MESSAGE_TYPE, General.MessageType.ACTION.toString());
            messageToServer.put(General.MessageType.ACTION.toString(),General.Action_Type.VEHICLE_ACTION.toString());
            messageToServer.put(General.Action_Type.VEHICLE_ACTION.toString(), General.Vehicle_Actions.CHANGE_DIRECTION.toString());
            messageToServer.put(KEY_DRIVING_DIRECTION, drivingDirection.toString());
            if(mConnectionService.isConnectedToRemoteDevice())
                mConnectionService.sendMessageToRemoteDevice(General.getProtocolMessage(messageToServer.toString()));
        }catch (JSONException e){

        }

    }



    /**
     * Function get Steering direction based on rotation angle
     * @param rotation  - phone rotation angle around XY plane;
     * @return - Direction
     */
    private String getSteeringDirection(int rotation){
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
            messageToServer.put(General.MessageType.ACTION.toString(),General.Action_Type.VEHICLE_ACTION.toString());
            messageToServer.put(General.Action_Type.VEHICLE_ACTION.toString(), General.Vehicle_Actions.CHANGE_SPEED.toString());
            messageToServer.put(KEY_DIGITAL_SPEED, digitalSpeed);
            if(mConnectionService.isConnectedToRemoteDevice())
                mConnectionService.sendMessageToRemoteDevice(General.getProtocolMessage(messageToServer.toString()));
        }catch (JSONException e){

        }
    }

    /**
     * method sends steering angle to server
     * @param steerDirection - R = Right or L = Left
     * @param rotation       - Angle 0 - 40 degrees
     */
    private void sendSteeringAngleToServer(String steerDirection, double rotation){
        JSONObject messageToServer = new JSONObject();
        try {
            messageToServer.put(General.KEY_MESSAGE_TYPE, General.MessageType.ACTION.toString());
            messageToServer.put(General.MessageType.ACTION.toString(),General.Action_Type.VEHICLE_ACTION.toString());
            messageToServer.put(General.Action_Type.VEHICLE_ACTION.toString(), General.Vehicle_Actions.STEERING.toString());
            if(rotation < MIN_STEERING_ANGLE)
                rotation = MIN_STEERING_ANGLE;
            else if(rotation > MAX_STEERING_ANGLE)
                rotation = MAX_STEERING_ANGLE;
            messageToServer.put(KEY_STEERING_DIRECTION, steerDirection);

            // convert the rotation angle to 0-40
            if(steerDirection.equals(STEERING_RIGHT))
                rotation = STRAIGHT_STEERING_ANGLE - rotation;
            else
                rotation -= STRAIGHT_STEERING_ANGLE;
            messageToServer.put(KEY_ROTATION_ANGLE, rotation);
            if(mConnectionService.isConnectedToRemoteDevice())
                mConnectionService.sendMessageToRemoteDevice(General.getProtocolMessage(messageToServer.toString()));
        }catch (JSONException e){

        }
    }


}

