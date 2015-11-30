package BL.Bluetooth;

/**
 * Created by Administrator on 11/27/2015.
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


    public static class Message {
        private MessageType messageType;
        private String msg;

        public Message() {
        }

        public Message(MessageType messageType, String msg){
            this.messageType = messageType;
            this.msg = msg;
        }

        public enum MessageType {
            DEBBUG, INFO, ERROR, ACTION
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }


}
