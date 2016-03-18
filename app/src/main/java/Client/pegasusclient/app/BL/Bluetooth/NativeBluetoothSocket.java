package client.pegasusclient.app.BL.Bluetooth;

import android.bluetooth.BluetoothSocket;
import client.pegasusclient.app.BL.Interfaces.IBluetoothSocketWrapper;

import java.io.*;

/**
 * @author  Tamir Sagi
 *
 * this class represent bluetooth mSocket including the various functionalities
 */
public class NativeBluetoothSocket implements IBluetoothSocketWrapper {

    private BluetoothSocket mSocket;
    private PrintWriter mWriter;

    public NativeBluetoothSocket(BluetoothSocket tmp) {
        this.mSocket = tmp;

        try {
            mWriter = new PrintWriter(mSocket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InputStream getInputStream()throws IOException{
        return mSocket.getInputStream();
    }

    @Override
    public PrintWriter getOutputStream()throws IOException{
        return mWriter;
    }

    @Override
    public String getRemoteDeviceName() {
        return mSocket.getRemoteDevice().getName();
    }

    @Override
    public void connect() throws IOException {
        mSocket.connect();
    }

    @Override
    public String getRemoteDeviceAddress() {
        return mSocket.getRemoteDevice().getAddress();
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    @Override
    public boolean isConnected(){
        if(mSocket != null)
            return mSocket.isConnected();
        return false;
    }

    @Override
    public BluetoothSocket getSocket() {
        return mSocket;
    }

}