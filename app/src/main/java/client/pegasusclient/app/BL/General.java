package client.pegasusclient.app.BL;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author  Tamir Sagi
 */
public class General {


    //Info
    public static final String OnBluetoothIsEnabled = "Bluetooth is Enabled";
    public static final String OnBluetoothIsTurningOn = "bluetooth turning on";

    //Error
    public static final String OnCloseSocketFailed = "close() of connectToRemoteDevice socket failed";
    public static final String OnWriteToSocketFailed = "Exception during write";
    public static final String OnDeviceConnectionLost = "Device connection was lost";
    public static final String OnCloseClientConnectionFailed = "unable to close() socket during connection failure";
    public static final String OnBluetoothNotSupported = "Bluetooth is not available";

    public static final String KEY_MESSAGE_TYPE = "MT";
    public static final String KEY_MESSAGE_SENDER = "sender";
    public static final String END_MESSAGE = "#";
    public static final String START_MESSAGE = "$";

    public enum MessageType{
        INFO, ERROR, WARNING, ACTION
    }

    public enum Vehicle_Control{
        MANUAL, AUTONOMOUS
    }

    public enum Action_Type{
        VEHICLE_ACTION, SETTINGS
    }

    public enum Vehicle_Actions {
        CHANGE_SPEED,STEERING, CHANGE_DIRECTION
    }


    public enum Receiver {
        PEGASUS_SERVER(0), LOGS(1), Live_Streaming(2);

        private int value;

        public static HashMap<Integer,Receiver> receivers = new HashMap<Integer, Receiver>();

        static{
            for(Receiver r : EnumSet.allOf(Receiver.class))
                receivers.put(r.getValue(),r);
        }

        Receiver(int value){
            this.value = value;
        }


        public int getValue(){
            return value;
        }

        public static Receiver get(int value){
            return receivers.get(value);
        }
    }

    /**
     * Method add $ and # according to the protocol before and after the message
     * $ = Start Message
     * # = Message Ends
     * @param msg
     * @return Fixed Message
     */
    public static String getProtocolMessage(String msg){

        return /*START_MESSAGE + */ msg + END_MESSAGE;
    }


}
