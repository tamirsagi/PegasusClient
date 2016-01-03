package client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth;

import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;

/**
 * Created by Administrator on 1/3/2016.
 */
public interface BluetoothDevicesListListener {

    /**
     * get the bluetooth device to connect
     * @param btDeviceAddress - bluetooth mac address
     */
    void getBluetoothDeviceToConnect(String btDeviceAddress);


}
