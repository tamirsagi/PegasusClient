package client.pegasusclient.app.UI.Activities;

import android.content.*;
import android.os.IBinder;
import client.pegasusclient.app.BL.Services.ConnectionManager;
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


    private ConnectionManager mConnectionManager;       //Bluetooth conneciton service

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mConnectionManager != null)
            getApplicationContext().unbindService(BluetoothServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mConnectionManager != null)
         createBinnedConnectionManagerService();
    }



    /**
     * Bind to Bluetooth Connection Manager Service
     */
    private void createBinnedConnectionManagerService() {
        Intent connectionManagerServiceIntent = new Intent(this, ConnectionManager.class);
        getApplicationContext().bindService(connectionManagerServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        startService(connectionManagerServiceIntent);
    }

    /**
     * Create the service instance
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionManager.MyLocalBinder gpsBinder = (ConnectionManager.MyLocalBinder) service;
            mConnectionManager = gpsBinder.gerService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(BluetoothServiceConnection != null)
                getApplicationContext().unbindService(BluetoothServiceConnection);
        }
    };
}
