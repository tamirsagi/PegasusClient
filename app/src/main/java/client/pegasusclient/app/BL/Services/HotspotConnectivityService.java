package client.pegasusclient.app.BL.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.*;
import android.widget.Toast;

/**
 * @author Tamir Sagi
 *         The service is responsible to update Manual control fragment with relevant phone positioning
 *         both rotation and inclination
 */
public class HotspotConnectivityService extends Service {
    private static final String TAG = "HotspotConnectivityService";
    private static String WIFI_STATE_CHANGED = "android.net.wifi.STATE_CHANGE";
    private static String CONNECTION_STATE_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";

    private static final String mPegasusHotspotSSID = "Pegasus_AP";
    private static final String mPegasusHotspotPass = "Pp123456";
    private static final int PEGASUS_AP_PRIORITY = 40;

    private WifiConfiguration mPegasusHotspotConfiguration;
    private WifiManager mWifiManager;
    private EHotSpotStatus mEHotSpotStatus = EHotSpotStatus.DEFAULT;
    private int mNetID;

    /**
     * connectivity states
     */
    public enum EHotSpotStatus {
        DEFAULT,
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }


    /*  Bind the Client which is the  Activity to the service
        We use an Object for that   */
    private final IBinder HotspotConnectivityService = new MyLocalBinder();

    private Handler mHandler;


    @Override
    public IBinder onBind(Intent intent) {
        setHotSpotStatus(EHotSpotStatus.DISCONNECTED);
        initializeWifiConfiguration();
        registerToBroadcast();
        return HotspotConnectivityService;
    }


    /**
     * change hotspot connection status
     *
     * @param aStatus - new status
     */
    private void setHotSpotStatus(EHotSpotStatus aStatus) {
        mEHotSpotStatus = aStatus;
    }

    /**
     * @return current connectivity state
     */
    public EHotSpotStatus getEHotSpotStatus() {
        return mEHotSpotStatus;
    }

    /**
     * initialize Wifi configuration
     */
    private void initializeWifiConfiguration() {
        mPegasusHotspotConfiguration = new WifiConfiguration();
        mPegasusHotspotConfiguration.priority = PEGASUS_AP_PRIORITY;
        mPegasusHotspotConfiguration.SSID = "\"" + mPegasusHotspotSSID + "\"";
        mPegasusHotspotConfiguration.wepKeys[0] = "\"" + mPegasusHotspotPass + "\"";
        mPegasusHotspotConfiguration.wepTxKeyIndex = 0;
        mPegasusHotspotConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        mPegasusHotspotConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        mPegasusHotspotConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        mPegasusHotspotConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        mPegasusHotspotConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        mPegasusHotspotConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        mPegasusHotspotConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        mPegasusHotspotConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        mPegasusHotspotConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        mPegasusHotspotConfiguration.preSharedKey = "\"" + mPegasusHotspotPass + "\"";
        //initialize wifi manger
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //save given netID
        mNetID = mWifiManager.addNetwork(mPegasusHotspotConfiguration);
    }

    /**
     * Connect to Pegasus AP
     *
     * @return
     */
    public boolean connectToPegasusAP() throws Exception {
        if (mWifiManager != null && !mEHotSpotStatus.equals(EHotSpotStatus.CONNECTED)) {
            if (!mWifiManager.isWifiEnabled()) {
                setHotSpotStatus(EHotSpotStatus.CONNECTING);
                Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
                mWifiManager.setWifiEnabled(true);
            }
            mWifiManager.enableNetwork(mNetID, true);
            if (mWifiManager.reconnect()) {
                setHotSpotStatus(EHotSpotStatus.CONNECTED);
                return true;
            }
    }
    setHotSpotStatus(EHotSpotStatus.DISCONNECTED);
    throw new  Exception("wifi manager is null");
}

    /**
     * disconnect from any wifi
     *
     * @throws Exception
     */
    public void disconnect() throws Exception {
        if (mWifiManager != null) {
            mWifiManager.disconnect();
            setHotSpotStatus(EHotSpotStatus.DISCONNECTED);
        } else
            throw new Exception("wifi manager is null");
    }


    /**
     * Register an handler to get messages from service;
     *
     * @param handler
     */
    public void registerHandler(Handler handler) {
        mHandler = handler;
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    /**
     * register to wifi and network changes broadcast receiver
     */
    private void registerToBroadcast() {
        //register to broadcast receiver #1 filter
        IntentFilter wifi_state_changed_filter = new IntentFilter(WIFI_STATE_CHANGED);
        getApplication().registerReceiver(mWifiBroadCastReceiver, wifi_state_changed_filter);
        //register to broadcast receiver #2 filter
        IntentFilter connection_state_changed_filter = new IntentFilter(WIFI_STATE_CHANGED);
        getApplication().registerReceiver(mWifiBroadCastReceiver, connection_state_changed_filter);
    }


    /**
     * Handle wifi and network changes here
     */
    private BroadcastReceiver mWifiBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WIFI_STATE_CHANGED)) {


            } else if (action.equals(CONNECTION_STATE_CHANGED)) {


            }
        }
    };

public class MyLocalBinder extends Binder {

    public HotspotConnectivityService gerService() {
        return HotspotConnectivityService.this;
    }
}

}


