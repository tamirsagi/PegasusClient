package client.pegasusclient.app.UI.Fragments.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import client.pegasusclient.app.UI.Activities.R;
import client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth.MainBluetoothFragment;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author Tamir Sagi
 *         <p/>
 *         This class is a fragment for settings.
 */
public class ClientSettings extends Fragment implements RecyclerViewClickListener {

    public static final String PAGE_NUMBER = "PAGE NUMBER";
    private String[] mSettingsListTitles = {"Network Connections"};
    private int[] mSettingsIconIDs = {R.drawable.setting_connection_icon};
    private RecyclerView mSettingList;
    private SettingsRecyclerAdapter mSettingAdapter;
    private View root;


    public static ClientSettings newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(PAGE_NUMBER, page);
        ClientSettings fragment = new ClientSettings();
        fragment.setArguments(args);
        return fragment;
    }

    public enum Settings {

        NetworkConnection(0);
        private static final java.util.Map<Integer, Settings> lookup
                = new HashMap<Integer, Settings>();

        static {
            for (Settings w : EnumSet.allOf(Settings.class))
                lookup.put(w.getNumVal(), w);
        }

        private int code;

        Settings(int code) {
            this.code = code;
        }

        public int getNumVal() {
            return code;
        }

        public static Settings get(int code) {
            return lookup.get(code);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.client_settings, container, false);
        setSettingsList(root);

        return root;
    }


    private void setSettingsList(View view) {

        mSettingList = (RecyclerView)view.findViewById(R.id.settings_list);
        mSettingList.setHasFixedSize(true);
        mSettingList.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Setting adapter
        mSettingAdapter = new SettingsRecyclerAdapter(getContext(),this,mSettingsListTitles,mSettingsIconIDs);
        mSettingList.setAdapter(mSettingAdapter);

    }

    @Override
    public void getClickedItemPosition(int position) {
        switch(Settings.get(position)){
            case NetworkConnection:
                showRelevantSettingFragment(new MainBluetoothFragment());
                break;
        }
    }


    private void showRelevantSettingFragment(Fragment newFragment){
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.setting_container, newFragment); //     setting_container is your FrameLayout container
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

}
