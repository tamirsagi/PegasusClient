package client.pegasusclient.app.UI.Fragments.MainApp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;
import client.pegasusclient.app.UI.Fragments.settings.ClientSettings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tamir Sagi
 * This class holds the pages.
 */
public class MainAppPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private static final String tabTitles[] = new String[]{"Settings", "Autonomous", "Manual Control", "Logs", "Camera", "Map"};

    public MainAppPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }


    public enum PAGES {

        Settings(0), Autonomous(1), Manual_Control(2), Logs(3),
        Camera(4), Map(5);

        private static final Map<Integer, PAGES> lookup
                = new HashMap<Integer, PAGES>();

        static {
            for (PAGES w : EnumSet.allOf(PAGES.class))
                lookup.put(w.getNumVal(), w);
        }

        private int code;

        PAGES(int code) {
            this.code = code;
        }

        public int getNumVal() {
            return code;
        }

        public static PAGES get(int code) {
            return lookup.get(code);
        }
    }


    @Override
    public Fragment getItem(int position) {
        switch (PAGES.get(position)) {
            case Settings:
                return ClientSettings.newInstance(position);
//            case Autonomous:
//                break;
//            case Manual_Control:
//                break;
//            case Logs:
//                break;
//            case Camera:
//                break;
//            case Map:
//                break;
        }
        return new Fragment();
    }

    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_UNCHANGED;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];

    }
}
