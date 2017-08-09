package org.ndk.eventinjector;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.ndk.eventinjector.utils.KeyTouchInjector2;

public class InjectorUInputActivity extends AppCompatActivity {

    private static final String TAG = "InjectorUInputActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_injector_uinput);
    }

    public void onCreateVir(View view) {
        KeyTouchInjector2.initInjector(Build.VERSION.SDK_INT);
    }

    public void onDestroyVir(View view) {
        KeyTouchInjector2.destroy();
    }


    public void onSendKey(View view) {
        KeyTouchInjector2.sendKey();
    }

    public void onSendTouch(View view) {
        KeyTouchInjector2.sendTouch();

    }

    public void onSendSwipe(View view) {
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
