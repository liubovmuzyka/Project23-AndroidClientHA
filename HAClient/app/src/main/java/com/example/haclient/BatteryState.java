package com.example.haclient;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.StrictMode;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.TimerTask;

public class BatteryState extends TimerTask {

    final MainActivity activity;
    private Context context;
    IntentFilter ifilter;
    Intent batteryStatus;
    float batteryState;

    private static final String url_battery = ":8088/updateBatteryState?batteryState=";
    //private static final String url_battery = "http://136.199.55.234:8088/updateBatteryState?batteryState=";
    public BatteryState(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, ifilter);

    }

    public boolean isCharging(){
        int status = context.registerReceiver(null, ifilter).getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
        return isCharging;
    }

    public boolean typeOfCharging(){
        int chargePlug = context.registerReceiver(null, ifilter).getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        return usbCharge;
    }

    public float currentBatteryLevel(){
        int level = context.registerReceiver(null, ifilter).getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = context.registerReceiver(null, ifilter).getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;
        batteryState = batteryPct;
        return batteryPct;
    }

    @Override
    public void run() {
        float currentBatteryLevel = currentBatteryLevel();
        try {
            setStateBattery(currentBatteryLevel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (currentBatteryLevel>95){
            //System.out.println("FULL Battery");
//            activity.runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(activity.getBaseContext(),  "FULL Battery: " + currentBatteryLevel, Toast.LENGTH_SHORT).show();
//                }
//            });
        }
        boolean isCharging = isCharging();
        if(isCharging){
//            activity.runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(activity.getBaseContext(),  "Charging", Toast.LENGTH_SHORT).show();
//                }
//            });
            //System.out.println("Charging");
        } else {
//            activity.runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(activity.getBaseContext(),  "Not Charging", Toast.LENGTH_SHORT).show();
//                }
//            });
            //System.out.println("Not Charging");
        }
    }

    private String setStateBattery(float state) throws IOException, JSONException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        URL obj = new URL(MainActivity.ip + url_battery + state);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("content-type", "application/json");

        String inline = "";
        Scanner scanner = new Scanner(httpURLConnection.getInputStream());

        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }
        scanner.close();
        System.out.println("Battery: " + inline + "%");
        return inline;

    }
}
