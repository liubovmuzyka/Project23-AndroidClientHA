package com.example.haclient;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.StrictMode;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationsManager2 extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();

    private static final String url_email = ":8088/updateStateEmail?stateEmail=";
    //private static final String url_email = "http://136.199.55.234:8088/updateStateEmail?stateEmail=";
    private static final String url_count_email = ":8088/updateEmailCount?countEmail=";
    //private static final String url_count_email = "http://136.199.55.234:8088/updateEmailCount?countEmail=";

    TextView countNotifications;


    public void onCreate() {
        super.onCreate();
        System.out.println("Created NotificationManager Object");



       // countNotifications = (TextView) activity.findViewById(R.id.notifikations);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        System.out.println("Connected!");
        System.out.println("All active Notifications: ");
        for (StatusBarNotification statusBarNotification : getActiveNotifications()) {
            System.out.println("Notification: " + statusBarNotification.toString());
            if (statusBarNotification.getPackageName().equals("com.google.android.gm")){
                if (statusBarNotification.getId() != 0) {
                    synchronized (emailIds) {
                        emailIds.add(statusBarNotification.getId());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        System.out.println("Betreff: " + statusBarNotification.getNotification().extras.getCharSequence("android.text").toString());
                    }
                }
            }
        }
        try {
            setCountEmail();
//            countNotifications.post(new Runnable(){
//                @Override
//                public void run(){
//                    countNotifications.setText("Emails received: " + emailIds.size());
//                }
//            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onListenerDisconnected() {
        System.out.println("Disconnected!");
    }

    public static Set<Integer> emailIds = new HashSet<>();


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        System.out.println("********** onNotificationPosted");
        if (sbn.getPackageName().equals("com.google.android.gm")){
            try {
                setStateEmail("on");
                if (sbn.getId() != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        System.out.println("Betreff: " + sbn.getNotification().extras.getCharSequence("android.text").toString());
                    }
                    synchronized (emailIds) {
                        emailIds.add(sbn.getId());
                    }
                }
                setCountEmail();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());
        System.out.println("**********  onNotificationPosted");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        System.out.println("********** onNotificationRemoved");
        if (sbn.getPackageName().equals("com.google.android.gm")){
            try {
                setStateEmail("off");
                synchronized (emailIds) {
                    emailIds.remove(sbn.getId());
                }
                setCountEmail();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());
        System.out.println( "********** onNOtificationRemoved");
    }

    private String setStateEmail(String onOrOff) throws IOException, JSONException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        URL obj = new URL(MainActivity.ip + url_email + onOrOff);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("content-type", "application/json");

        String inline = "";
        Scanner scanner = new Scanner(httpURLConnection.getInputStream());

        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }
        scanner.close();
        System.out.println(inline);
        return inline;

    }

    private String setCountEmail() throws IOException, JSONException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        int count;
        synchronized (emailIds) {
            count = emailIds.size();
        }

        URL obj = new URL(MainActivity.ip + url_count_email + count);
        System.out.println("Set email count over REST API (new count: " + count + ")");
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("content-type", "application/json");

        String inline = "";
        Scanner scanner = new Scanner(httpURLConnection.getInputStream());

        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }
        scanner.close();
        //System.out.println(inline);
        return inline;

    }
}
