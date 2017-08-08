package org.ndk.eventinjector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
}
