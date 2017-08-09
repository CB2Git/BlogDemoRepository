package org.ndk.eventinjector;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import org.ndk.eventinjector.utils.KeyTouchInjector;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDemo2(View v) {
        Intent intent = new Intent(this, InjectorInputActivity.class);
        startActivity(intent);
    }

    public void onDemo1(View v) {
        Intent intent = new Intent(this, InjectorEventXActivity.class);
        startActivity(intent);
    }

    public void onDemo3(View view) {
        Intent intent = new Intent(this, InjectorUInputActivity.class);
        startActivity(intent);
    }

    /**
     * 使用这个方法只能在当前应用中注入，如果想注入到全局，必须拥有系统前面(root权限无效！！！)
     *
     * @param view
     */
    public void OnDemo4(View view) {
        moveTaskToBack(false);
        Toast.makeText(this, "后台注入...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                KeyTouchInjector.checkSU();
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
            }
        }).start();

    }
}
