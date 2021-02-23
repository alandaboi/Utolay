package com.AXC.Utolay;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.AXC.Utolay.Helper.OverlayTouchListener;

public class ForegroundService extends Service {

    private static final String CHANNEL_ID = "Utolay";
    private Context context, mainContext;
    protected static MediaPlayer mediaPlayer, music;
    private static WindowManager windowManager;
    private static WindowManager.LayoutParams params;
    private ImageView imageView;
    private static int size;
    protected static boolean bgm, mute;
    protected static boolean alive;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mainContext = getApplicationContext();
        mediaPlayer = new MediaPlayer();
        size = 250;
        bgm = MainActivity.sharedPreferences.getBoolean("bgm", true);
        mute = MainActivity.sharedPreferences.getBoolean("mute", false);
        alive = true;
        Log.v("Application Log", "Service onCreate()");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageView != null) {
            int temp = -params.x;
            params.x = -params.y;
            params.y = temp;
            windowManager.updateViewLayout(imageView, params);
            MainActivity.sharedPreferences.edit().putInt("x", params.x).apply();
            MainActivity.sharedPreferences.edit().putInt("y", params.y).apply();

            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            MainActivity.x = size.x;
            MainActivity.y = size.y;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (imageView != null)
            windowManager.removeView(imageView);
        if (music != null && music.isPlaying())
            music.stop();
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
        MainActivity.sharedPreferences.edit().putBoolean("service", false).apply();
        alive = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Application Log", "Service onStartCommand()");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Intent pauseI = new Intent(this, ForegroundService.class);
        pauseI.setAction("Pause");
        PendingIntent pauseIntent = PendingIntent.getService(this, 0,
                pauseI, 0);
        Intent quitI = new Intent(this, ForegroundService.class);
        quitI.setAction("Quit");
        PendingIntent quitIntent = PendingIntent.getService(this, 0,
                quitI, 0);
        String action = intent.getAction();
        if (action != null) {
            if (action.equalsIgnoreCase("quit")) {
                if (mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                stopSelf();
                return START_NOT_STICKY;
            } else if (action.equalsIgnoreCase("pause")) {
                if (imageView != null)
                    windowManager.removeView(imageView);
                if (mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                if (music != null && music.isPlaying())
                    music.stop();
                Intent startI = new Intent(this, ForegroundService.class);
                startI.setAction("Start");
                PendingIntent startIntent = PendingIntent.getService(this, 0,
                        startI, 0);
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("What's up Tenshimp?")
                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_baseline_pause_24, "Start", startIntent)
                        .addAction(R.drawable.ic_baseline_close_24, "Quit", quitIntent)
                        .build();
                startForeground(1, notification);
                return START_NOT_STICKY;
            } else if (action.equalsIgnoreCase("Start")) {
                if (bgm) {
                    music = MediaPlayer.create(this, R.raw.bgm);
                    music.setVolume(0.420f, 0.420f);
                    music.setLooping(true);
                    music.start();
                }
                if (imageView != null)
                    windowManager.addView(imageView, params);
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("What's up Tenshimp?")
                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_baseline_pause_24, "Pause", pauseIntent)
                        .addAction(R.drawable.ic_baseline_close_24, "Quit", quitIntent)
                        .build();
                startForeground(1, notification);
                return START_NOT_STICKY;
            }
        }
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("What's up Tenshimp?")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_baseline_pause_24, "Pause", pauseIntent)
                .addAction(R.drawable.ic_baseline_close_24, "Quit", quitIntent)
                .build();
        startForeground(1, notification);
        startPowerOverlay();
        if (bgm) {
            music = MediaPlayer.create(this, R.raw.bgm);
            music.setVolume(0.420f, 0.420f);
            music.setLooping(true);
            music.start();
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void startPowerOverlay() {
        // Starts the button overlay.
        windowManager = (WindowManager) mainContext.getSystemService(WINDOW_SERVICE);
        imageView = new ImageView(mainContext);
        imageView.setImageResource(R.drawable.uto);
        //mediaPlayer = MediaPlayer.create(context, R.raw.nice_to_meet_you);
        //mediaPlayer.start();
        Log.v("Application Log", "Image Set");
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // APPLICATION_OVERLAY FOR ANDROID 26+ AS THE PREVIOUS VERSION RAISES ERRORS
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            // FOR PREVIOUS VERSIONS USE TYPE_PHONE AS THE NEW VERSION IS NOT SUPPORTED
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.NO_GRAVITY;
        params.x = MainActivity.sharedPreferences.getInt("x", 0);
        params.y = MainActivity.sharedPreferences.getInt("y", 0);
        params.height = size;
        params.width = size;
        imageView.setOnTouchListener(new OverlayTouchListener(mediaPlayer, windowManager, params, imageView, mute, context, size, LAYOUT_FLAG));
        windowManager.addView(imageView, params);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
