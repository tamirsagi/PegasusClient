package client.pegasusclient.app.BL.common.constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author  Tamir Sagi
 */
public class MessageKeys {





    public static final String KEY_MESSAGE_SENDER = "sender";
    public static final String END_MESSAGE = "#";
    public static final String START_MESSAGE = "$";

    public static final String KEY_MESSAGE_TYPE = "MT";
    public static final int MESSAGE_TYPE_INFO = 1000;
    public static final int MESSAGE_TYPE_ACTION = 2000;
    public static final int MESSAGE_TYPE_SETTINGS = 3000;
    public static final int MESSAGE_TYPE_ERROR = 4000;


    public static final String KEY_VEHICLE_ACTION_TYPE = "VA";
    public static final int MESSAGE_TYPE_ACTION_CHANGE_DIRECTION = 2001;
    public static final int MESSAGE_TYPE_ACTION_CHANGE_SPEED = 2002;
    public static final int MESSAGE_TYPE_ACTION_STEERING = 2003;


    public static final String KEY_MESSAGE_TYPE_SETTINGS = "ST";
    public static final String KEY_VEHICLE_MODE = "VM";
    public static final int  VEHICLE_MODE_AUTONOMOUS = 0;
    public static final int  VEHICLE_MODE_MANUAL = 1;

    public static final String KEY_AUTONOMOUS_MODE = "AM";
    public static final int  AUTONOMOUS_MODE_AUTO_DRIVE = 0;
    public static final int  AUTONOMOUS_MODE_FIND_PARKING = 1;

    public static final String KEY_RECEIVED_MESSAGE_TYPE_LOG = "log";
    public static final String KEY_RECEIVED_MESSAGE_TYPE_REAL_TIME_DATA = "rtd";

    /**
     * Method add $ and # according to the protocol before and after the message
     * $ = Start Message
     * # = Message Ends
     * @param msg
     * @return Fixed Message
     */
    public static String getProtocolMessage(String msg){

        return String.format("%s%s%s",START_MESSAGE ,msg,END_MESSAGE);
    }

    public static JSONObject getVehicleModeMessage(int aMode) throws JSONException {
        JSONObject vehicleMode = new JSONObject();
        vehicleMode.put(KEY_MESSAGE_TYPE,MESSAGE_TYPE_SETTINGS);
        vehicleMode.put(KEY_MESSAGE_TYPE_SETTINGS,KEY_VEHICLE_MODE);
        vehicleMode.put(KEY_VEHICLE_MODE,aMode);
        return vehicleMode;
    }


    public static JSONObject getAutonomousModeMessage(int aAutonomousMode) throws JSONException {
        JSONObject autonomousMode = new JSONObject();
        autonomousMode.put(KEY_MESSAGE_TYPE,MESSAGE_TYPE_SETTINGS);
        autonomousMode.put(KEY_MESSAGE_TYPE_SETTINGS,KEY_AUTONOMOUS_MODE);
        autonomousMode.put(KEY_AUTONOMOUS_MODE,aAutonomousMode);
        return autonomousMode;
    }


}
