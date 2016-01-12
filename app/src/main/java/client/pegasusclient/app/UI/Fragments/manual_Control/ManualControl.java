package client.pegasusclient.app.UI.Fragments.manual_Control;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import client.pegasusclient.app.UI.Activities.R;

/**
 * @author  Tamir Sagi
 * This class controls the Pegasus Vehicle using Gyro and linear acceleration
 */
public class ManualControl extends Fragment {

    private View root;



    public static ManualControl newInstance() {
        ManualControl mc = new ManualControl();
        return mc;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_manual_control, container, false);





        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
