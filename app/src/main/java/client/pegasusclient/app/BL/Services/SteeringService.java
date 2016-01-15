package client.pegasusclient.app.BL.Services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import java.text.DecimalFormat;


/**
 * @author Tamir Sagi
 *         This class manages accelerator sensor service. it's binned when timer starts(On Game Activity)
 *         It keeps updating the phone angle and sends broadcast messages back to Game activity.
 */
public class SteeringService extends Service implements SensorEventListener {

    public static final String TAG = "Steering Service";

    public static final String INTENT_FILTER_NAME = "POSITION_SERVICE NOTIFIER";
    public static final String BUNDLE_DATA_ADD_MINES = "phone's angle was deviated for long time, mines added to board";
    private static final int MILI = 1000;
    private static final int SECONDS_MINUTE = 60;
    private static final int RADIANS_PI = 180;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private final int minAngleDeviation = 10;



    //Bind the Client which is the game Activity to the service
    //We use an Object for that
    private final IBinder positioningKeeper = new MyLocalBinder();

    private Sensor accelerometer;
    private SensorManager sensorManager;
    private boolean work = false;                        //determines whether we should send the data or not

    public SteeringService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        setAccelerationSensor();
        return positioningKeeper;
    }

    private void setAccelerationSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null)
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String position, action, dAngle;
        position = "X:" + decimalFormat.format(event.values[0]) + "\nY:" + decimalFormat.format(event.values[1]) +
                "\nZ:" + decimalFormat.format(event.values[2]);
        double radians = (Math.atan2(event.values[0], event.values[1])); //radians
        double vAngle = radians * (RADIANS_PI / Math.PI);                      //radians to degrees
        dAngle = "angle : " + decimalFormat.format(vAngle);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



    //stop service from updating with position and restart values
    public void stopUpdatingCurrentAngle() {

    }


    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }


    public void registerListener() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    //we need to extends Binder in order to make our object as a binder
    public class MyLocalBinder extends Binder {

        public SteeringService gerService() {
            return SteeringService.this;
        }
    }

}


