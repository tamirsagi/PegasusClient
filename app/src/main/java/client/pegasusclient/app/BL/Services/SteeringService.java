package client.pegasusclient.app.BL.Services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;

import java.text.DecimalFormat;
import java.util.HashMap;


/**
 * @author Tamir Sagi
 *         The service is responsible to update Manual control fragment with relevant phone positionin
 *         It keeps the last values and compare it with the current updated value, if the different is more than
 *         3 degrees it sends it over the fragment.
 */
public class SteeringService extends Service implements SensorEventListener {

    public static final String TAG = "Steering Service";
    public static final int Sender_SteeringService = 1;

    public static final String KEY_INCLINATION = "Inclination";
    public static final String KEY_ROTATION = "Rotation";

    private static final int MIN_ANGLE_TO_SEND = 3;

    private Sensor accelerometer;
    private SensorManager sensorManager;                 // sensor manager, we get the specific service

    /*  Bind the Client which is the game Activity to the service
        We use an Object for that   */
    private final IBinder mSteeringService = new MyLocalBinder();


    private HashMap<String, Handler> handlers = new HashMap<String, Handler>();

    private int mLastInclinationValue;
    private int mLastRotationValue;
    private boolean mShouldSendData;


    @Override
    public IBinder onBind(Intent intent) {
        setAccelerationSensor();
        return mSteeringService;
    }

    private void setAccelerationSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        else if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null)
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] g = event.values.clone();   //get the vector returns by the accelerometer sensor event values

        double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);

        // Normalize the accelerometer vector
        g[0] /= norm_Of_g;
        g[1] /= norm_Of_g;
        g[2] /= norm_Of_g;

        int inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));
        int rotation = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[1])));

        if (Math.abs(rotation - mLastRotationValue) >= MIN_ANGLE_TO_SEND) {
            mLastRotationValue = rotation;
            mShouldSendData = true;
        }

        for (String key : handlers.keySet()) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_INCLINATION, inclination);
            bundle.putInt(KEY_ROTATION, mLastRotationValue);
            msg.what = Sender_SteeringService;
            msg.setData(bundle);
            handlers.get(key).sendMessage(msg);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    /**
     * UnRegister sensor's listener
     */
    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Register sensor's listener
     */
    public void registerListener() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    /**
     * Register an handler to get messages from service;
     *
     * @param handler
     */
    public void registerHandler(String name, Handler handler) {
        handlers.put(name, handler);
    }

    /**
     * Register an handler to get messages from service;
     *
     * @param name - handler to delete
     */
    public void unregisterHandler(String name) {
        if (handlers.containsKey(name))
            handlers.remove(name);
    }


    public class MyLocalBinder extends Binder {

        public SteeringService gerService() {
            return SteeringService.this;
        }
    }


}


