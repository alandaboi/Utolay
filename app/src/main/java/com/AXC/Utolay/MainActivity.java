package com.AXC.Utolay;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import static com.AXC.Utolay.ForegroundService.alive;
import static com.AXC.Utolay.ForegroundService.mediaPlayer;
import static com.AXC.Utolay.ForegroundService.music;

public class MainActivity extends AppCompatActivity {

    protected static SharedPreferences sharedPreferences;
    @SuppressLint("StaticFieldLeak")
    protected static Context context;
    protected static int x, y;
    protected static ImageView imageView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MyPref", 0);
        context = getApplicationContext();
        Helper.main();
        imageView = findViewById(R.id.toggle);
        if (sharedPreferences.getBoolean("service", false))
            imageView.setImageResource(R.drawable.uto_skin);
        else
            imageView.setImageResource(R.drawable.uto_skin_gray);
        imageView.setDrawingCacheEnabled(true);
        imageView.setOnTouchListener((v, event) -> {
            Bitmap bmp = Bitmap.createBitmap(imageView.getDrawingCache());
            int color = bmp.getPixel((int) event.getX(), (int) event.getY());
            Log.v("USERINFO", color + "");
            if (color == Color.TRANSPARENT)
                return false;
            Log.v("USERINFO", event.toString());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.v("USERINFO", "uto'd");
                if (!sharedPreferences.getBoolean("service", false)) {
                    Log.v("USERINFO", "start");
                    startService(new Intent(context, ForegroundService.class));
                    sharedPreferences.edit().putBoolean("service", true).apply();
                    imageView.setImageResource(R.drawable.uto_skin);
                } else {
                    Log.v("USERINFO", "stop");
                    stopService(new Intent(context, ForegroundService.class));
                    sharedPreferences.edit().putBoolean("service", false).apply();
                    imageView.setImageResource(R.drawable.uto_skin_gray);
                }
                return true;
            }
            return false;
        });

        Switch bgm = findViewById(R.id.bgm);
        bgm.setChecked(sharedPreferences.getBoolean("bgm", true));
        bgm.setOnCheckedChangeListener((v, b) -> {
            Log.v("USERINFO", "bgm'd");
            sharedPreferences.edit().putBoolean("bgm", b).apply();
            if (b && alive) {
                music = MediaPlayer.create(this, R.raw.bgm);
                music.setVolume(0.420f, 0.420f);
                music.setLooping(true);
                music.start();
            } else {
                if (music != null && music.isPlaying())
                    music.stop();
            }
        });

        Switch mute = findViewById(R.id.mute);
        mute.setChecked(sharedPreferences.getBoolean("mute", false));
        mute.setOnCheckedChangeListener((v, b) -> {
            Log.v("USERINFO", "muted");
            sharedPreferences.edit().putBoolean("mute", b).apply();
            ForegroundService.mute = b;
            if (mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.stop();
        });

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        x = size.x;
        y = size.y;

        // Check for overlay permission. If not enabled, request for it. If enabled, show the overlay
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Missing Permissions")
                    .setMessage("This App Requires Screen Overlay Permission to run.")
                    .setPositiveButton("Agree", (dialog, which) -> {
                        CharSequence text = "Please grant the access to the application.";
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", getPackageName(), null)));
                    })
                    .setNegativeButton("No thanks", (dialog, which) -> {
                        finishAndRemoveTask();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();
            alertDialog.show();
            }
    }
}