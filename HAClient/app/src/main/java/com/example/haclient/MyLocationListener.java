package com.example.haclient;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executor;

public class MyLocationListener extends AppCompatActivity implements LocationListener {

    private String latitude, longtitude;
    final MainActivity activity;
    private Context context;

    private static final String url = ":8088/updateGPS?";
    //private static final String url = "http://136.199.55.234:8088/updateGPS?";
    public MyLocationListener( MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
        //setMyLastLocation();
    }

    @Override
    public void onLocationChanged(Location location) {

        setLatitude(String.valueOf(location.getLatitude()));
        setLongtitude(String.valueOf(location.getLongitude()));

        //System.out.println("Provider: " + location.getProvider());
        //System.out.println("Latitude: " + String.valueOf(location.getLatitude()));
        //System.out.println("Longitude: " + String.valueOf(location.getLongitude()));
        Toast.makeText(activity.getApplicationContext(),"Location changed", Toast.LENGTH_SHORT).show();
        try {
            System.out.println("new state of the lamp after changed location: " + sendGPStoServer());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String sendGPStoServer() throws IOException, JSONException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        URL obj = new URL(MainActivity.ip + url + "latitude="+ getLatitude() + "&"+ "longtitude=" + getLongtitude());
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("content-type", "application/json");

        String inline = "";
        Scanner scanner = new Scanner(httpURLConnection.getInputStream());

        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }
        scanner.close();
        return inline;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("onStatusChanged: ",  "Do something with the status: " + status );
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("onProviderEnabled: ", "Do something with the provider-> " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("onProviderDisabled:", "Do something with the provider-> " + provider);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }


    private void setMyLastLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener((Executor) context, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double longi = location.getLongitude();
                    LatLng latLng = new LatLng(lat, longi);
                    Log.d("location", "MyLastLocation coordinat :" + latLng);
                    System.out.println("MyLastLocation coordinat :" + latLng);
                }
            }
        });
    }
}
