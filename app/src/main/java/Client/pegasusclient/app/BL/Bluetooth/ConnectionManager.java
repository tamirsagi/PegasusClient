package client.pegasusclient.app.BL.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Tamir Sagi
 *         This class handle conneciton to remote device
 *         it keeps a inner class which extends a thread to handle the connection read and write
 */
public class ConnectionManager {

    public static ConnectionManager connectionService;

    private final UUID mSharedUUID = UUID.fromString("00000000-0000-0000-0000-000000001101");
    // Debugging
    private static final String TAG = "ConnectionService";
    private static final boolean debugging = true;

    // Member fields
    private final BluetoothAdapter mAdapter;            //device bluetooth adapter
    private Handler mHandler;

    private ConnectionService mConnectionService;
    private int mState;

    /**
     * Standard SerialPortService ID
     * the UUID on the client must match one that the other device (server mode) is listening for
     */

    /**
     * A bluetooth can support up to 7 connections. This array holds 7 unique UUIDs.
     * When attempting to make a connection, . When accepting incoming connections server listens for all 7 UUIDs.
     */

    // Constants that indicate the current connection state
    public static final int STATE_DISCONNECTED = -1;           //we are disconnected
    public static final int STATE_NONE = 0;                     // we're doing nothing
    public static final int STATE_CONNECTING = 1;               // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;                // now setConnectionManager to a remote device

    /**
     * Constructor. Prepares a new Bluetooth Connection Service session.
     */
    private ConnectionManager() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mConnectionService = new ConnectionService();
    }

    public static ConnectionManager getConnectionServiceInstance() {
        if (connectionService == null) {
            connectionService = new ConnectionManager();
        }
        return connectionService;
    }

    /**
     * @param handler A Handler to send messages back to the UI Activity
     */
    public void setHandler(Handler handler) {
        mHandler = handler;
    }


    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private void setState(int state) {
        if (debugging)
            Log.d(TAG, "setState() " + mState + " -> " + state);

        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public int getState() {
        return mState;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param remoteDevice The BluetoothDevice to connectToRemoteDevice
     */
    public boolean connectToRemoteDevice(BluetoothDevice remoteDevice) {
        if (debugging) Log.d(TAG, "connect to: " + remoteDevice);
        if (mConnectionService.isConnected())
            disconnectFromRemoteDevice();           //if socket is exist disconnect from it
        try {
            setState(STATE_CONNECTING);
            mConnectionService.connectToRemoteDevice(remoteDevice, mSharedUUID);
            mConnectionService.start();             //start the thread to handle socket
            return true;
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            return false;
        }
    }

    /**
     * Stop connection manager thread
     */
    public void disconnectFromRemoteDevice() {
        if (debugging)
            Log.d(TAG, "disconnect FromRemote Device");
        setState(STATE_DISCONNECTED);
        if (mConnectionService != null) {
            mConnectionService.closeSocket();
            mConnectionService = null;
        }
    }

    public void sendMessageToRemoteDevice(String msg) {
        mConnectionService.writeToSocket(msg);
    }


    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        if (debugging)
            Log.d(TAG, "connection Failed");
        setState(STATE_DISCONNECTED);
        if (mConnectionService != null) {
            mConnectionService.closeSocket();
            mConnectionService = null;
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_CONNECTING);
        // Send a failure message back to the Activity
        //  updateActivity(General.MessageType.ERROR, General.OnDeviceConnectionLost);
    }


//    private void updateActivity(General.MessageType messageType, String msg) {
//        Message message = new Message();
//        message.
//        Bundle bundle = new Bundle();
//        bundle.putString(messageType.toString(), msg);
//        message.setData(bundle);
//        mHandler.sendMessage(message);
//    }

    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }


    /**
     * Check whether we are connected to remote device or not
     *
     * @return
     */
    public boolean isConnectedToRemoteDevice() {
        if (mConnectionService != null)
            return mConnectionService.isConnected();
        return false;
    }

    /**
     * @return remote bluetooth device
     */
    public BluetoothDevice getRemoteBluetoothDevice() {
        return mConnectionService.getRemoteBluetoothDevice();
    }


    /**
     *     Handle connection is a separate Thread
     */


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device.the connection either
     * succeeds or fails.
     */
    private class ConnectionService extends Thread {

        private BluetoothDevice mRemoteDevice;    //remote device which we connect to
        private IBluetoothSocketWrapper bluetoothSocket;
        private UUID mUUID;

        /**
         * Connect to Bluetooth Device
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
            if (bluetoothSocket.getSocket() != null)
                // Always cancel discovery because it will slow down a connection
                mAdapter.cancelDiscovery();
            try {
                connectToSocket();
                setState(STATE_CONNECTED);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                throw new IOException(TAG + " Could not connect to Socket",e);
            }
        }

        @Override
        public void run() {
            while (bluetoothSocket.isConnected()) {
                try {
                    readFromSocket();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected: " + e.getMessage());
                    connectionLost();
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
                JSONObject jsonFromString = new JSONObject(receivedMessage);
                Message messageToHandle = new Message();
                Log.i(TAG, "receivedMessage: " + receivedMessage);
            }
        }


        /**
         * Write to the setConnectionManager OutStream.
         *
         * @param msg The bytes to writeToSocket
         */
        public void writeToSocket(String msg) {
            try {
                bluetoothSocket.getOutputStream().write(msg.getBytes());
                // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(BluetoothActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (Exception e) {
                Log.e(TAG, General.OnWriteToSocketFailed, e);
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
                Log.e(TAG, General.OnCloseSocketFailed, e);
            }
        }

//        /**
//         * when I receive that exception, I instantiate a fallback BluetoothSocket,
//         * As you can be seen, invoking the hidden method createRfcommSocket via reflections.
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

        /**
         * @return connection state
         */
        public boolean isConnected() {
            if(bluetoothSocket != null)
                return bluetoothSocket.isConnected();
            return false;
        }

        /**
         * @return the remote device we are connected to
         */
        public BluetoothDevice getRemoteBluetoothDevice() {
            return mRemoteDevice;
        }

    }


}//end of file

