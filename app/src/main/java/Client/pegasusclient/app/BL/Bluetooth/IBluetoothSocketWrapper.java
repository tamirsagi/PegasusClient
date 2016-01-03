package client.pegasusclient.app.BL.Bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.*;

public interface IBluetoothSocketWrapper {

    InputStream getInputStream()throws IOException ;

    OutputStream getOutputStream() throws IOException;

    String getRemoteDeviceName();

    void connect() throws IOException;

    String getRemoteDeviceAddress();

    void close() throws IOException;


    boolean isConnected();

    BluetoothSocket getSocket();

}