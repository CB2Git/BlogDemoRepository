package org.ndk.eventinjector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import org.ndk.eventinjector.utils.KeyTouchInjector;
import org.ndk.eventinjector.utils.ShellUtils;

/**
 * 直接使用input命令，不管是使用java调用命令还是使用jni调用，都很慢(平均1500ms)
 */
public class InjectorInputActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private static final String TAG = "InjectorInputActivity";

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_injector_input);
        mEditText = (EditText) findViewById(R.id.main_edit_text);
        mEditText.setOnFocusChangeListener(this);
    }

    private long firstTime = 0;

    public void onSendKey(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                firstTime = System.currentTimeMillis();
                ShellUtils.execCommand("input keyevent KEYCODE_POWER", true);
                //or ShellUtils.execCommand("input keyevent 26", true);
                Log.i(TAG, "run time(ms): " + (System.currentTimeMillis() - firstTime));
            }
        }).start();
    }

    public void onSendInNative(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                firstTime = System.currentTimeMillis();
                KeyTouchInjector.execCommand("input keyevent KEYCODE_VOLUME_DOWN", true);
                Log.i(TAG, "run time(ms): " + (System.currentTimeMillis() - firstTime));
            }
        }).start();
    }

    public void onSendTouch(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                firstTime = System.currentTimeMillis();
                ShellUtils.execCommand("input tap 300 500", true);
                Log.i(TAG, "run time(ms): " + (System.currentTimeMillis() - firstTime));
            }
        }).start();
    }

    public void onSendSwipe(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                firstTime = System.currentTimeMillis();
                ShellUtils.execCommand("input swipe  300 500 400 600", true);
                Log.i(TAG, "run time(ms): " + (System.currentTimeMillis() - firstTime));
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    firstTime = System.currentTimeMillis();
                    ShellUtils.execCommand("input text 1234", true);
                    Log.i(TAG, "run time(ms): " + (System.currentTimeMillis() - firstTime));
                }
            }).start();
        }
    }
}
