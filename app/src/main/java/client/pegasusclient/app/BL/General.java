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


    public enum MessageType{
        INFO, ERROR, WARNING, ACTION
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


}
