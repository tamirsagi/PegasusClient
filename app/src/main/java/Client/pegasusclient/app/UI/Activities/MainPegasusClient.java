package client.pegasusclient.app.UI.Activities;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_pegasus_client);

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
}
