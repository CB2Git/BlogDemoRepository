package org.ndk.eventinjector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.ndk.eventinjector.utils.KeyTouchInjector;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onScan(null);
    }

    public void onScan(View v) {
        if (KeyTouchInjector.init()) {
            Log.i(TAG, "KeyTouchInjector init ok");
        } else {
            Log.i(TAG, "KeyTouchInjector init error");
        }
    }

    public void onSendKey(View v) {
        KeyTouchInjector.injectKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
    }

    public void onSendTouch(View v) {
        KeyTouchInjector.injectTouchEvent(510, 700);
    }

    public void onSendSwipe(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                KeyTouchInjector.injectSwipeEvent(new int[]{
                        98, 681,
                        102, 681,
                        104, 681,
                        112, 681,
                        117, 681,
                        128, 681,
                        131, 681,
                        142, 681,
                        98, 681,
                        102, 681,
                        104, 681,
                        112, 681,
                        117, 681,
                        128, 681,
                        131, 681,
                        142, 681});
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        KeyTouchInjector.recovery();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int action = event.getAction();
        StringBuffer sb = new StringBuffer();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                sb.append("action_down");
                break;
            case MotionEvent.ACTION_MOVE:
                sb.append("action_move");
                break;
            case MotionEvent.ACTION_UP:
                sb.append("action_up");
                break;
        }
        sb.append("\tonTouchEvent: x=" + x + "y = " + y);
        Log.i(TAG, "onTouchEvent: " + sb);
        return super.onTouchEvent(event);
    }
}
