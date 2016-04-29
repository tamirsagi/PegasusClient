package client.pegasusclient.app.BL.Bluetooth.constants;

/**
 * Created by Administrator on 4/29/2016.
 */
public class BluetoothMessages {

    //Info
    public static final String OnBluetoothIsEnabled = "Bluetooth is Enabled";
    public static final String OnBluetoothIsTurningOn = "bluetooth turning on";

    //Error
    public static final String OnCloseSocketFailed = "close() of connectToRemoteDevice socket failed";
    public static final String OnWriteToSocketFailed = "Exception during write";
    public static final String OnDeviceConnectionLost = "Device connection was lost";
    public static final String OnCloseClientConnectionFailed = "unable to close() socket during connection failure";
    public static final String OnBluetoothNotSupported = "Bluetooth is not available";
}
