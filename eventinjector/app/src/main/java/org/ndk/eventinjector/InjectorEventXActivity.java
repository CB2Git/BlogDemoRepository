package org.ndk.eventinjector;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.ndk.eventinjector.utils.KeyTouchInjector;

/**
 * 测试读写/input/dev/eventX测试Demo
 */
public class InjectorEventXActivity extends AppCompatActivity {

    private static final String TAG = "InjectorEventXActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_injectoreventxactivity);
        //onScan(null);
    }

    public void onScan(View v) {
        if (KeyTouchInjector.init()) {
            Log.i(TAG, "KeyTouchInjector init ok");
            Toast.makeText(this, "初始化ok", Toast.LENGTH_SHORT).show();
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
        moveTaskToBack(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(3000);
                KeyTouchInjector.injectSwipeEvent(new int[]{
                        000, 700,
                        300, 700});
                SystemClock.sleep(3000);
                KeyTouchInjector.injectSwipeEvent(new int[]{
                        000, 700,
                        300, 700});
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
