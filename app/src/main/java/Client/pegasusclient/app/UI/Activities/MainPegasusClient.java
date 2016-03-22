package client.pegasusclient.app.UI.Activities;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.util.Log;
import client.pegasusclient.app.BL.Services.ConnectionService;
import client.pegasusclient.app.BL.Services.HotspotConnectivityService;
import client.pegasusclient.app.UI.Fragments.MainApp.MainAppPagerAdapter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

/**
 * This Class manages the pager elements including pages and tabs
 * Its the main screen when app is initiated
 */
public class MainPegasusClient extends FragmentActivity {


    private ConnectionService mConnectionService;       //Bluetooth connection service
    private boolean mHasBTServiceStarted;

    private HotspotConnectivityService mHotspotConnectivityService;
    private boolean mHashHotspotServiceStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_pegasus_client);

        createBinnedConnectionManagerService(); //bind to Bluetooth connection manager service

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pagesViewer);
        final MainAppPagerAdapter adapter = new MainAppPagerAdapter(getSupportFragmentManager()
                , MainPegasusClient.this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                              @Override
                                              public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                              }

                                              @Override
                                              public void onPageSelected(int position) {

                                              }

                                              @Override
                                              public void onPageScrollStateChanged(int state) {
                                              }
                                          }
        );

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killBluetoothConnectionService();
        killHotspotConnectivityService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mConnectionService != null)
            getApplicationContext().unbindService(BluetoothServiceConnection);
        if(mHotspotConnectivityService !=null)
            getApplicationContext().unbindService(hotspotConnectivityService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mConnectionService != null)
         createBinnedConnectionManagerService();
        if(mHotspotConnectivityService != null)
            createBinnedHotspotConnectivityServiceService();
    }



    /**
     * Bind to Bluetooth Connection Manager Service
     */
    private void createBinnedConnectionManagerService() {
        Intent connectionManagerServiceIntent = new Intent(this, ConnectionService.class);
        getApplicationContext().bindService(connectionManagerServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        if(!mHasBTServiceStarted) {
            startService(connectionManagerServiceIntent);
            mHasBTServiceStarted = true;
        }
    }

    /**
     * Create the service instance
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.MyLocalBinder connectionManager = (ConnectionService.MyLocalBinder) service;
            mConnectionService = connectionManager.gerService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(BluetoothServiceConnection != null)
                getApplicationContext().unbindService(BluetoothServiceConnection);
        }
    };

    /**
     * destroy bluetooth connection service
     */
    private void killBluetoothConnectionService(){
        if(mConnectionService != null) {
            mConnectionService.stopSelf();      //kill connection service
            mConnectionService = null;
            mHasBTServiceStarted = false;
        }
    }

    /**
     * Bind to Hotspot Connection Manager Service
     */
    private void createBinnedHotspotConnectivityServiceService() {
        Intent intent = new Intent(this, HotspotConnectivityService.class);
        getApplicationContext().bindService(intent, hotspotConnectivityService, Context.BIND_AUTO_CREATE);
        if(!mHashHotspotServiceStarted){
            startService(intent);
            mHasBTServiceStarted = true;
        }
    }

    /**
     * Create the service instance
     */
    private ServiceConnection hotspotConnectivityService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HotspotConnectivityService.MyLocalBinder hotspotConnectivityService = (HotspotConnectivityService.MyLocalBinder) service;
            mHotspotConnectivityService = hotspotConnectivityService.gerService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (hotspotConnectivityService != null ) {
                getApplicationContext().unbindService(hotspotConnectivityService);
            }
        }
    };

    /**
     * destroy bluetooth connection service
     */
    private void killHotspotConnectivityService(){
        if(mHotspotConnectivityService != null) {
            mHotspotConnectivityService.stopSelf();      //kill connection service
            mHotspotConnectivityService = null;
            mHashHotspotServiceStarted = false;
        }
    }
}
