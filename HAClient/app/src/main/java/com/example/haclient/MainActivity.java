package com.example.haclient;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.SwitchPreference;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.PowerManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    MyLocationListener mylistener;
    Location location;
    String state;
    MusicManager musicManager;
    BrightnessManager brightnessManager;

    public static volatile String ip = "http://192.168.178.198";
    private static volatile String url_HA = ":8123";
    //private static final String url_HA = "http://136.199.55.234:8123";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!foregroundServiceRunning()) {
            Intent serviceIntent = new Intent(this, MyService.class);
            startForegroundService(serviceIntent);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {        //ask for permission
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        }
        boolean settingsCanWrite = Settings.System.canWrite(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                if (!settingsCanWrite) {
                    intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                }
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }


//        Intent mServiceIntent = new Intent(this, NotificationsManager2.class);
//        startService(mServiceIntent);
//
//        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));

        setContentView(R.layout.activity_main);

        mylistener = new MyLocationListener(MainActivity.this, this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            System.out.println("LOCATION_SERVICE Available");
            Toast.makeText(getApplicationContext(),"LOCATION_SERVICE Available", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(),"LOCATION_SERVICE NOT Available", Toast.LENGTH_SHORT).show();
            System.out.println("LOCATION_SERVICE Available");
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, mylistener);
        location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        brightnessManager = new BrightnessManager(MainActivity.this, this);
        brightnessManager.updateBrightness(255);

        musicManager = new MusicManager(MainActivity.this, this);
        try {
            state = musicManager.getStateMusic();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        timerOn_Alarm();

        timerOn_Battery();
        SensorsManager sensorsManager = new SensorsManager(MainActivity.this, this);

        Button buttonHA = (Button) findViewById(R.id.webinterfaceHA);
        buttonHA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ip + url_HA));
                startActivity(browserIntent);
            }
        });

        Button buttonHomeScreen = (Button) findViewById(R.id.homescreen);
        buttonHomeScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
        });

        Button buttonWhiteBoard = (Button) findViewById(R.id.whiteboard);
        buttonWhiteBoard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.smarttech.notebookplayer");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(MainActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonSensors= (Button) findViewById(R.id.sensors);
        buttonSensors.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SensorManager mySensorManager = (SensorManager) MainActivity.this.getSystemService(SENSOR_SERVICE);

                Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                if (lightSensor != null) {
                    System.out.println("Sensor.TYPE_LIGHT Available");
                    Toast.makeText(getApplicationContext(),"Sensor.TYPE_LIGHT Available", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(),"Sensor.TYPE_LIGHT NOT Available", Toast.LENGTH_SHORT).show();
                    System.out.println("Sensor.TYPE_LIGHT NOT Available");
                }
                if (mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
                    Toast.makeText(getApplicationContext(),"STEP COUNTER sensor supports", Toast.LENGTH_SHORT).show();
                    System.out.println("STEP COUNTER sensor supports");
                } else {
                    Toast.makeText(getApplicationContext(),"no STEP COUNTER sensor supports", Toast.LENGTH_SHORT).show();
                    System.out.println("no STEP COUNTER sensor supports");
                }
                if (locationManager != null) {
                    System.out.println("LOCATION_SERVICE Available");
                    Toast.makeText(getApplicationContext(),"LOCATION_SERVICE Available", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(),"LOCATION_SERVICE NOT Available", Toast.LENGTH_SHORT).show();
                    System.out.println("LOCATION_SERVICE Available");
                }
            }
        });

        Button buttonDateien = (Button) findViewById(R.id.datein);
        buttonDateien.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.documentsui");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(MainActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
                }
                //String path = Environment.getExternalStorageState() + "/";
                //Uri uri = Uri.parse(path);
                //Intent intent = new Intent(Intent.ACTION_PICK);
                //intent.setDataAndType(uri, "*/*");
                //startActivity(intent);
            }
        });

        TextView notifikations = (TextView) findViewById(R.id.notifikations);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (NotificationsManager2.emailIds) {
                            notifikations.setText("" + NotificationsManager2.emailIds.size());
                        }
                    }
                });
            }
        }, 0, 500);

    }

    public boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void timerOn_Alarm() {
        Timer timer = new Timer();
        TimerTask task = new MusicManager(MainActivity.this, this);
        timer.schedule(task, 0, 2000);
    }

    public void timerOn_Battery() {
        Timer timer = new Timer();
        TimerTask task = new BatteryState(MainActivity.this, this);
        timer.schedule(task, 0, 10000);
    }

}
