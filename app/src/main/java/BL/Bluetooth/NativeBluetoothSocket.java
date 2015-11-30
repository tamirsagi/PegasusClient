package BL.Bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.*;

/**
 * @author  Tamir Sagi
 *
 * this class represent bluetooth socket including the various functionalities
 */
public class NativeBluetoothSocket implements IBluetoothSocketWrapper {

    private BluetoothSocket socket;

    public NativeBluetoothSocket(BluetoothSocket tmp) {
        this.socket = tmp;
    }

    @Override
    public InputStream getInputStream()throws IOException{
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream()throws IOException{
        return socket.getOutputStream();
    }

    @Override
    public String getRemoteDeviceName() {
        return socket.getRemoteDevice().getName();
    }

    @Override
    public void connect() throws IOException {
        socket.connect();
    }

    @Override
    public String getRemoteDeviceAddress() {
        return socket.getRemoteDevice().getAddress();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public boolean isConnected(){
        return socket.isConnected();
    }

    @Override
    public BluetoothSocket getSocket() {
        return socket;
    }

}