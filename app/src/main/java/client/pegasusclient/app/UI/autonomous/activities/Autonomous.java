package client.pegasusclient.app.UI.autonomous.activities;

import android.content.*;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import client.pegasusclient.app.BL.common.constants.MessageKeys;
import client.pegasusclient.app.UI.Activities.MainApp;
import client.pegasusclient.app.UI.Activities.R;
import client.pegasusclient.app.UI.autonomous.constants.AutonomousMessageParam;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Tamir Sagi
 *         This class controls the Pegasus Vehicle manually using Gyro and linear acceleration
 *         digital speed on Arduino(0-255)
 *         0-40 degrees Steering Angle each side
 */
public class Autonomous extends AppCompatActivity {

    public static final String TAG = Autonomous.class.getSimpleName();


    // UI
    private ImageButton mAutoDrive;
    private ImageButton mFindParking;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autonomous);
        initialAutoDriveButton();
        initialFindParkingButton();
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


    private void initialAutoDriveButton() {
        mAutoDrive = (ImageButton) findViewById(R.id.fab_auto_drive);
        mAutoDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject msg = null;
                try {
                    if (MainApp.CONNECTION_SERVICE.isConnectedToRemoteDevice()) {
                        msg = MessageKeys.getAutonomousModeMessage(MessageKeys.AUTONOMOUS_MODE_AUTO_DRIVE);
                        MainApp.CONNECTION_SERVICE.sendMessageToRemoteDevice(MessageKeys.getProtocolMessage(msg.toString()));
                        final Intent autoDrive = new Intent(v.getContext(), VehicleMode.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt(MessageKeys.KEY_AUTONOMOUS_MODE, MessageKeys.AUTONOMOUS_MODE_AUTO_DRIVE);
                        autoDrive.putExtras(bundle);
                        startActivity(autoDrive);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void initialFindParkingButton() {
        mFindParking = (ImageButton) findViewById(R.id.fab_find_parking);
        mFindParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject msg = null;
                try {
                    if (MainApp.CONNECTION_SERVICE.isConnectedToRemoteDevice()) {
                        msg = MessageKeys.getAutonomousModeMessage(MessageKeys.AUTONOMOUS_MODE_FIND_PARKING);
                        msg.put(AutonomousMessageParam.JSON_KEY_PARKING_TYPE,AutonomousMessageParam.PARKING_TYPE_PARALLEL_RIGHT);
                        MainApp.CONNECTION_SERVICE.sendMessageToRemoteDevice(MessageKeys.getProtocolMessage(msg.toString()));
                        final Intent findParking = new Intent(v.getContext(), VehicleMode.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt(MessageKeys.KEY_AUTONOMOUS_MODE, MessageKeys.AUTONOMOUS_MODE_FIND_PARKING);
                        findParking.putExtras(bundle);
                        startActivity(findParking);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}

