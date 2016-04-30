package client.pegasusclient.app.UI.autonomous.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import client.pegasusclient.app.BL.Util.General;
import client.pegasusclient.app.BL.common.constants.MessageKeys;
import client.pegasusclient.app.UI.Activities.MainApp;
import client.pegasusclient.app.UI.Activities.R;
import client.pegasusclient.app.UI.autonomous.adapters.VehicleLog;
import client.pegasusclient.app.UI.autonomous.adapters.VehicleLogAdapter;
import client.pegasusclient.app.UI.autonomous.constants.AutonomousMessageParam;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by Tamir on 4/29/2016.
 */
public class VehicleMode extends AppCompatActivity {

    private static final String TAG = VehicleMode.class.getSimpleName();
    private static final String AUTONOMOUS_MODE_AUTO_DRIVE = "Auto Drive";
    private static final String AUTONOMOUS_MODE_FIND_PARKING = "Find Parking";

    private DecimalFormat DECIMAL_FORMATTER =  new DecimalFormat(".##");

    private TextView mVehicleMode;
    private TextView mCurrentSpeed;
    private TextView mTotalDistance;
    private ListView mLogs;
    private VehicleLogAdapter mLogsAdapter;
    private Handler mHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_mode);

        mLogs = (ListView) findViewById(R.id.autonomous_vehicle_logs);
        mLogsAdapter = new VehicleLogAdapter(getApplicationContext(), R.layout.vehicle_log);

        mVehicleMode = (TextView) findViewById(R.id.autonomous_mode);
        Bundle extra = getIntent().getExtras();
        setMode(extra.getInt(MessageKeys.KEY_AUTONOMOUS_MODE));
        mCurrentSpeed = (TextView) findViewById(R.id.current_speed_value);

        mTotalDistance = (TextView) findViewById(R.id.autonomous_distance_value);

        setHandler();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainApp.CONNECTION_SERVICE.removeHandler(TAG);

    }

    @Override
    public void onStart() {
        super.onStart();
        MainApp.CONNECTION_SERVICE.registerToMeesagesService(TAG,mHandler);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setMode(int aMode) {
        switch (aMode) {
            case MessageKeys.AUTONOMOUS_MODE_AUTO_DRIVE:
                mVehicleMode.setText(AUTONOMOUS_MODE_AUTO_DRIVE);
                break;
            case MessageKeys.AUTONOMOUS_MODE_FIND_PARKING:
                mVehicleMode.setText(AUTONOMOUS_MODE_FIND_PARKING);
                break;
        }
    }

    private void setSpeed(double aSpeed) {
        mCurrentSpeed.setText(String.format("%s (m/s)", DECIMAL_FORMATTER.format(aSpeed)));
    }

    private void setDistance(double aDistance) {
        mTotalDistance.setText(String.format("%s (m)", DECIMAL_FORMATTER.format(aDistance)));
    }

    private void setHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                try {
                    JSONObject json_data = General.convertBundleToJson(data);
                    String messageType = json_data.getString(MessageKeys.KEY_MESSAGE_TYPE);
                    if (messageType.equals(MessageKeys.KEY_RECEIVED_MESSAGE_TYPE_LOG)) {
                        VehicleLog vl = new VehicleLog(json_data);
                        mLogsAdapter.addItem(mLogsAdapter.getCount(), vl);
                    } else if (messageType.equals(MessageKeys.KEY_RECEIVED_MESSAGE_TYPE_REAL_TIME_DATA)) {
                        handleRealTimeData(json_data);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        };
    }


    /**
     * set UI for incoming data
     * @param aJsonObject
     * @throws JSONException
     */
    private void handleRealTimeData(JSONObject aJsonObject) throws JSONException {
        int dataType = aJsonObject.getInt(AutonomousMessageParam.JSON_KEY_REAL_TIME_DATA_TYPE);
        switch (dataType){
            case AutonomousMessageParam.JSON_REAL_TIME_TYPE_DISTANCE:
                double currentDistance = aJsonObject.getDouble(AutonomousMessageParam.JSON_KEY_DISTANCE);
                setDistance(currentDistance);
                break;
            case AutonomousMessageParam.JSON_REAL_TIME_TYPE_SPEED:
                double currentSpeed = aJsonObject.getDouble(AutonomousMessageParam.JSON_KEY_SPEED);
                setSpeed(currentSpeed);
                break;
            case AutonomousMessageParam.JSON_REAL_TIME_TYPE_SENSOR:
                break;
        }
    }


}
