package com.AXC.Utolay.Helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.AXC.Utolay.MainActivity;
import com.AXC.Utolay.R;

import java.util.Timer;
import java.util.TimerTask;

public class OverlayTouchListener implements View.OnTouchListener {

    public OverlayTouchListener(MediaPlayer mediaPlayer, WindowManager windowManager, WindowManager.LayoutParams params, ImageView imageView, boolean mute, Context context, int size, int LAYOUT_FLAG) {
        this.mediaPlayer = mediaPlayer;
        this.windowManager = windowManager;
        this.params = params;
        this.imageView = imageView;
        this.mute = mute;
        this.context = context;
        this.size = size;
        this.LAYOUT_FLAG = LAYOUT_FLAG;
    }

    private MediaPlayer mediaPlayer;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams params;
    private final ImageView imageView;
    private final boolean mute;
    private final Context context;
    private final int size;
    private final int LAYOUT_FLAG;

    private WindowManager.LayoutParams youtubeParams, twitterParams;
    private ImageView youtube, twitter;


    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private long latestPressTime = 0;
    private long previousPressTime = 0;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Save current x/y
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                if (latestPressTime == 0 || latestPressTime + 500 < System.currentTimeMillis()) {
                    if (latestPressTime != 0) {
                        previousPressTime = latestPressTime;
                    }
                    latestPressTime = System.currentTimeMillis();
                } else {
                    if (mediaPlayer != null && mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() > latestPressTime + 100
                        && youtubeParams == null && twitterParams == null
                        && initialX == params.x && initialY == params.y) {
                    youtube = new ImageView(context);
                    youtube.setImageResource(R.drawable.youtube_icon);
                    youtubeParams = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            LAYOUT_FLAG,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT);
                    youtubeParams.x = params.x + (params.x >= 0 ? -1 : 1) * size * 2/3;
                    youtubeParams.y = params.y;
                    youtubeParams.height = 150;
                    youtubeParams.width = 150;
                    youtube.setOnTouchListener(new RedirectTouchListener("https://www.youtube.com/channel/UCdYR5Oyz8Q4g0ZmB4PkTD7g", context));
                    windowManager.addView(youtube, youtubeParams); // add youtube redirect
                    twitter = new ImageView(context);
                    twitter.setImageResource(R.drawable.twitter_icon);
                    twitterParams = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            LAYOUT_FLAG,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT);
                    twitterParams.x = params.x;
                    twitterParams.y = params.y + (params.y >= 0 ? -1 : 1) * size * 2/3;
                    twitterParams.height = 150;
                    twitterParams.width = 150;
                    twitter.setOnTouchListener(new RedirectTouchListener("https://twitter.com/amatsukauto", context));
                    windowManager.addView(twitter, twitterParams); // add twitter redirect
                    ImageView finalYoutube = youtube;
                    ImageView finalTwitter = twitter;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            windowManager.removeView(finalYoutube);
                            windowManager.removeView(finalTwitter);
                            youtube = null;
                            twitter = null;
                            twitterParams = null;
                            youtubeParams = null;
                        }
                    }, 3000);
                } else if (System.currentTimeMillis() < latestPressTime + 100) {
                    if (!mediaPlayer.isPlaying() && !mute && youtubeParams == null && twitterParams == null) {
                        mediaPlayer = MediaPlayer.create(context, Helper.getSound());
                        mediaPlayer.start();
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                int travelX = (int) (event.getRawX() - initialTouchX);
                int travelY = (int) (event.getRawY() - initialTouchY);
                params.x = initialX + travelX;
                params.y = initialY + travelY;
                params.x = Math.min(params.x, MainActivity.x/2 - size/2);
                params.x = Math.max(params.x, -MainActivity.x/2 - size/2);
                params.y = Math.min(params.y, MainActivity.y/2 - size/2);
                params.y = Math.max(params.y, -MainActivity.y/2 - size/2);
                windowManager.updateViewLayout(imageView, params);
                if (youtubeParams != null) {
                    youtubeParams.x = params.x + (params.x >= 0 ? -1 : 1) * size * 2/3;
                    youtubeParams.y = params.y;
                    windowManager.updateViewLayout(youtube, youtubeParams);
                }
                if (twitterParams != null) {
                    twitterParams.x = params.x;
                    twitterParams.y = params.y + (params.y >= 0 ? -1 : 1) * size * 2/3;
                    windowManager.updateViewLayout(twitter, twitterParams);
                }
                MainActivity.sharedPreferences.edit().putInt("x", params.x).apply();
                MainActivity.sharedPreferences.edit().putInt("y", params.y).apply();
                if ((Math.abs(travelX) + Math.abs(travelY) > 10) && previousPressTime + 500 < System.currentTimeMillis()
                        && mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                return true;
        }
        return false;
    }

}
