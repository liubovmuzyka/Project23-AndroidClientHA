package com.example.haclient;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.widget.Toast;

public class SensorsManager {

    final MainActivity activity;
    private Context context;
    BrightnessManager brightnessManager;

    public SensorsManager(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
        brightnessManager = new BrightnessManager(activity, context);

        SensorManager mySensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            System.out.println("Sensor.TYPE_LIGHT Available");
            //Toast.makeText(activity.getApplicationContext(),"Sensor.TYPE_LIGHT Available", Toast.LENGTH_SHORT).show();
            mySensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            //Toast.makeText(activity.getApplicationContext(),"Sensor.TYPE_LIGHT NOT Available", Toast.LENGTH_SHORT).show();
            System.out.println("Sensor.TYPE_LIGHT NOT Available");
        }

        if (mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            //Toast.makeText(activity.getApplicationContext(),"STEP COUNTER sensor supports", Toast.LENGTH_SHORT).show();
            System.out.println("STEP COUNTER sensor supports");
            mySensorManager.registerListener(stepCountSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            //Toast.makeText(activity.getApplicationContext(),"no STEP COUNTER sensor supports", Toast.LENGTH_SHORT).show();
            System.out.println("no STEP COUNTER sensor supports");
        }
    }

    private int reportedSteps = 0;
    private int stepsTaken = 0;

    private final SensorEventListener stepCountSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                if (reportedSteps < 1) {
                    reportedSteps = (int) event.values[0];
                    System.out.println("reportedSteps:" + reportedSteps);
                }
                stepsTaken = (int) event.values[0] - reportedSteps;
                System.out.println("stepsTaken:" + stepsTaken);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private final SensorEventListener lightSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                if (event.values[0] <= 5.0) {
                    //System.out.println("LIGHT: " + event.values[0]);
                    //System.out.println("old Brightness: " + brightnessManager.getBrightness());
                    brightnessManager.updateBrightness(0);
                    //System.out.println(" and new Brightness: " + brightnessManager.updateBrightness(brightnessManager.getBrightness()));
                } else if (event.values[0] > 0.0 && event.values[0] !=255){
                    //System.out.println("LIGHT: " + event.values[0]);
                    //System.out.println("old Brightness: " + brightnessManager.getBrightness());
                    brightnessManager.updateBrightness(255);
                    //System.out.println(" and new Brightness: " + brightnessManager.updateBrightness(brightnessManager.getBrightness()));
                }
            }

        }
    };

}
