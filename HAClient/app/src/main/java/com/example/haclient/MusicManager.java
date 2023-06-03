package com.example.haclient;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.StrictMode;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.TimerTask;

class MusicManager extends TimerTask {
    final MainActivity activity;
    String state;
    private static final String url_music_state = ":8088/getStatusMusic";
    //private static final String url_music_state = "http://136.199.55.234:8088/getStatusMusic";
    private static final String url_music_off = ":8088/updateStatusMusic?stateMusic=off";
    //private static final String url_music_off = "http://136.199.55.234:8088/updateStatusMusic?stateMusic=off";
    private Context context;
    private MediaPlayer mediaPlayer;

    public MusicManager(MainActivity activity, Context context) {
        this.activity = activity;
        this.context=context;
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
    }

    public void run() {
        try {
            state = getStateMusic();
            if (state.contains("on")) {
                cancel();
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        startAlertDialog();
//                    }
//                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getStateMusic() throws IOException, JSONException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        URL obj = new URL(MainActivity.ip +url_music_state);
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

    private String setStatusMusic() throws IOException, JSONException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        URL obj = new URL(MainActivity.ip + url_music_off);
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

    public void startAlertDialog() {

        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.release();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Motion is detected!");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setNeutralButton("OK", (DialogInterface.OnClickListener) (dialog, which) -> {
            mediaPlayer.pause();
            try {
                setStatusMusic();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
