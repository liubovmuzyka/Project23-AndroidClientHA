package com.example.haclient;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.widget.Toast;

public class BrightnessManager {

    final MainActivity activity;
    private Context context;

    private float brightness;

    public BrightnessManager(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
        try{
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        }
        catch(Settings.SettingNotFoundException e){
            System.out.println("Cannot access system brightness");
            Toast.makeText(activity.getBaseContext(),  "Cannot access system brightness", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public float getBrightness() {
        return brightness;
    }


    public float updateBrightness(float newBrightness)  {
        brightness = newBrightness;
        Settings.System.putInt(context.getContentResolver(),  Settings.System.SCREEN_BRIGHTNESS, (int) brightness);
        return getBrightness();
    }

}
