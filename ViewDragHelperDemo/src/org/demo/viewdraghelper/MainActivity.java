
package org.demo.viewdraghelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_demo);
    }

    public void onClickDemo(View v) {
        Intent intent = new Intent(this, DragDemoActivity.class);
        startActivity(intent);
    }

    public void onClickSliding(View v) {
        Intent intent = new Intent(this, SlidingActivity.class);
        startActivity(intent);
    }
}
