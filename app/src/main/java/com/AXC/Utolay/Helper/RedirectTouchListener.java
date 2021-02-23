package com.AXC.Utolay.Helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class RedirectTouchListener implements View.OnTouchListener {

    private final Intent intent;
    private final Context context;

    public RedirectTouchListener(String url, Context context) {
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context = context;
    }

    //private long lastTouchTime = 0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //lastTouchTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                Log.v("Application Log", "Redirecting");
                context.startActivity(intent);
                return true;
        }
        return false;
    }
}
