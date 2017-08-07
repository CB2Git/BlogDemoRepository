package org.ndk.eventinjector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import org.ndk.eventinjector.utils.KeyTouchInjector;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String targetDevice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onScan(View v) {
        if (KeyTouchInjector.init()) {
            targetDevice = KeyTouchInjector.findKeyTouchDevice();
            Log.i(TAG, "onScan: " + targetDevice);
        }
    }

    public void onSendKey(View v) {
        KeyTouchInjector.injectKeyEvent("/dev/input/event2", KeyEvent.KEYCODE_VOLUME_DOWN);
    }

    @Override
    protected void onDestroy() {
        KeyTouchInjector.recovery();
        super.onDestroy();
    }
}
