package client.pegasusclient.app.BL.Bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * @author  Tamir Sagi
 * This class holds bluetooth device info for recycler list for Device List activity
 */
public class BluetoothDeviceInfo {

    private String mName;
    private String mAddress;
    private String mSignal;


    public BluetoothDeviceInfo(BluetoothDevice bt,int signal){
        setName(bt.getName());
        setAddress(bt.getAddress());
        setSignal("" + signal);
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getSignal() {
        return mSignal;
    }

    public void setSignal(String mSignal) {
        this.mSignal = mSignal;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

}

