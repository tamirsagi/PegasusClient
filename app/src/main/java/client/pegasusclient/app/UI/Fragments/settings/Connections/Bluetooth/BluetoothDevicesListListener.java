package client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth;

import android.view.View;
import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;

/**
 * Created by Administrator on 1/3/2016.
 */
public interface BluetoothDevicesListListener {

    /**
     * get the bluetooth device to connectToPegasusAP
     * @param btDeviceAddress - bluetooth mac address
     */
    void getBluetoothDeviceToConnect(View v, String btDeviceAddress);


}
