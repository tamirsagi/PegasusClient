package client.pegasusclient.app.BL.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import client.pegasusclient.app.BL.Bluetooth.constants.BluetoothMessages;
import client.pegasusclient.app.BL.Enums.EBluetoothStates;
import client.pegasusclient.app.BL.common.constants.MessageKeys;
import client.pegasusclient.app.BL.Interfaces.IBluetoothSocketWrapper;
import client.pegasusclient.app.BL.Bluetooth.NativeBluetoothSocket;
import client.pegasusclient.app.BL.Interfaces.onMessageReceivedListener;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Tamir Sagi
 *         This class handle conneciton to remote device over bluetooth
 *         it keeps a inner class which extends a thread to handle the connection read and write
 */
public class ConnectionService extends Service implements onMessageReceivedListener {

    private IBinder mConnectionManagerService = new MyLocalBinder();

    // Debugging
    private static final String TAG = "ConnectionService";
    private static final boolean debugging = true;

    // Member fields
    private BluetoothAdapter mAdapter;            //device bluetooth adapter

    //  private ConnectionManager CONNECTION_SERVICE = new ConnectionManager(this);
    private final UUID mSharedUUID = UUID.fromString("00000000-0000-0000-0000-000000001101");
    private ConnectionManager mConnectionManager;
    private EBluetoothStates mBluetoothConnectionState;
    private HashMap<String, Handler> handlers;       //used to keep handler for several fragments across the app
    /**
     * Standard SerialPortService ID
     * the UUID on the client must match one that the other device (server mode) is listening for
     */

    /**
     * A bluetooth can support up to 7 connections. This array holds 7 unique UUIDs.
     * When attempting to make a connection, . When accepting incoming connections server listens for all 7 UUIDs.
     */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnectionState = EBluetoothStates.DISCONNECTED;
        handlers = new HashMap<String, Handler>();
        mConnectionManager = new ConnectionManager();
        mConnectionManager.registerListener(this);
        return mConnectionManagerService;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public class MyLocalBinder extends Binder {

        public ConnectionService gerService() {
            return ConnectionService.this;
        }

    }

    /**
     * Register fragment to service in order to get messages from the server
     *
     * @param tag     fragment id
     * @param handler Handler to send messages
     */
    public void registerToMeesagesService(String tag, Handler handler) {
        handlers.put(tag, handler);
    }

    /**
     * remove handler from the map
     *
     * @param tag
     */
    public void removeHandler(String tag) {
        handlers.remove(tag);
    }


    /**
     * Set the current state of the chat connection
     *
     * @param btState An integer defining the current connection state
     */
    private void setState(EBluetoothStates btState) {
        if (debugging)
            Log.d(TAG, "setState() " + mBluetoothConnectionState + " -> " + btState);

        mBluetoothConnectionState = btState;
    }

    /**
     * Return the current connection state.
     */
    public EBluetoothStates getState() {
        return mBluetoothConnectionState;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param remoteDevice The BluetoothDevice to connectToRemoteDevice
     */
    public boolean connectToRemoteDevice(BluetoothDevice remoteDevice) {
        if (debugging) Log.d(TAG, "connectToPegasusAP to: " + remoteDevice);
        try {
            if (isConnectedToRemoteDevice() || socketStillAlive())
                disconnectFromRemoteDevice();           //if socket is exist disconnect from it
            setState(EBluetoothStates.CONNECTING);
            mConnectionManager.connectToRemoteDevice(remoteDevice, mSharedUUID);
            mConnectionManager.start();             //start the thread to handle socket
            setState(EBluetoothStates.CONNECTED);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    /**
     * Stop connection manager thread
     */
    public void disconnectFromRemoteDevice() {
        if (debugging)
            Log.d(TAG, "disconnect FromRemote Device");
        if (mConnectionManager != null && mConnectionManager.socketStillAlive()) {
            mConnectionManager.closeSocket();
            setState(EBluetoothStates.DISCONNECTED);
        }
    }

    public void sendMessageToRemoteDevice(String msg) {
        mConnectionManager.writeToSocket(msg);
    }


    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        if (debugging)
            Log.d(TAG, "connection Failed");
        setState(EBluetoothStates.DISCONNECTED);
        if (mConnectionManager != null) {
            mConnectionManager.closeSocket();
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(EBluetoothStates.CONNECTING);
        // Send a failure message back to the Activity
        //  updateActivity(General.MessageType.ERROR, General.OnDeviceConnectionLost);
    }

    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }


    /**
     * Check whether we are connected to remote device or not
     *
     * @return
     */
    public boolean isConnectedToRemoteDevice() {
        return mConnectionManager.isConnected();
    }

    public boolean socketStillAlive() {
        return mConnectionManager.socketStillAlive();
    }

    /**
     * @return remote bluetooth device
     */
    public BluetoothDevice getRemoteBluetoothDevice() {
        return mConnectionManager.getRemoteBluetoothDevice();
    }


    /**
     * Handle connection is a separate Thread
     */


    @Override
    public void onMessageReceived(String receivedMessage) {
        try {
            JSONObject jsonFromString = new JSONObject(receivedMessage);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device.the connection either succeeds or fails.
     */
    private class ConnectionManager extends Thread {

        private BluetoothDevice mRemoteDevice;    //remote device which we connectToPegasusAP to
        private IBluetoothSocketWrapper bluetoothSocket;
        private UUID mUUID;
        private onMessageReceivedListener mListener;



        public ConnectionManager() {
        }

        public void registerListener(onMessageReceivedListener listener) {
            mListener = listener;
        }

        /**
         * Connect to Bluetooth Device
         *
         * @param remoteDevice
         * @param uuidToTry
         * @return whether succeed or not
         */
        public void connectToRemoteDevice(BluetoothDevice remoteDevice, UUID uuidToTry) throws IOException {
            mRemoteDevice = remoteDevice;
            BluetoothSocket tmp = null;
            try {
                // Get a BluetoothSocket for a connection with the given BluetoothDevice (Insecure)
                tmp = mRemoteDevice.createInsecureRfcommSocketToServiceRecord(uuidToTry);

            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            bluetoothSocket = new NativeBluetoothSocket(tmp);
            mUUID = uuidToTry;

            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            try {
                connectToSocket();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                throw new IOException(TAG + " Could not connectToPegasusAP to Socket", e);
            }
        }

        @Override
        public void run() {
            while (bluetoothSocket.isConnected()) {
                try {
                    readFromSocket();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected: " + e.getMessage());
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Connect to current soccket and save input and output streams
         *
         * @throws IOException
         */
        private void connectToSocket() throws IOException {
            // This is a blocking call and will only return on a exception
            bluetoothSocket.connect();// Create a connection to the BluetoothSocket

        }

        /**
         * Read from the InputStream
         *
         * @throws IOException
         */
        public void readFromSocket() throws IOException, JSONException {
            int bytesAvailable = bluetoothSocket.getInputStream().available();
            if (bytesAvailable > 0) {
                byte[] msg = new byte[bytesAvailable];
                bluetoothSocket.getInputStream().read(msg);
                String receivedMessage = new String(msg);
                mListener.onMessageReceived(receivedMessage);        //fire the message to the service
                Log.i(TAG, "receivedMessage: " + receivedMessage);
            }
        }


        /**
         * Write to the setConnectionManager OutStream.
         *
         * @param msg The bytes to writeToSocket
         */
        public synchronized void writeToSocket(String msg) {
            try {
                bluetoothSocket.getOutputStream().println(msg);
            } catch (Exception e) {
                Log.e(TAG, BluetoothMessages.OnWriteToSocketFailed, e);
            }
        }

        /**
         * close current socket
         */
        public void closeSocket() {
            try {
                if (isConnected())
                    bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, BluetoothMessages.OnCloseSocketFailed, e);
            }
        }

        /**
         * @return connection state
         */
        public boolean isConnected() {
            if (bluetoothSocket != null)
                return bluetoothSocket.isConnected();
            return false;
        }

        /**
         * Method checks whether the socket is still alive, means not null
         *
         * @return
         */
        public boolean socketStillAlive() {
            if (bluetoothSocket != null)
                return bluetoothSocket.getSocket() != null;
            return false;
        }

        /**
         * @return the remote device we are connected to
         */
        public BluetoothDevice getRemoteBluetoothDevice() {
            return mRemoteDevice;
        }


        /**
         //         * when I receive that exception, I instantiate a fallback BluetoothSocket,
         //         * As can be seen, invoking the hidden method createRfcommSocket via reflections.
         //         *
         //         * @param current - current Socket
         //         */
//        public void setFallBackSocket(BluetoothSocket current) {
//            try {
//                Class<?> clazz = current.getRemoteDevice().getClass();
//                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
//                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
//                Object[] params = new Object[]{Integer.valueOf(1)};
//                bluetoothSocket = (IBluetoothSocketWrapper) m.invoke(current.getRemoteDevice(), params);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }

    }


}//end of file

