
package org.demo.viewdraghelper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class DragDemoActivity extends Activity {

    private TextView mFirstOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirstOne = (TextView) findViewById(R.id.tv_first_one);
        mFirstOne.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(DragDemoActivity.this, "点击", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i("ViewDragHelper1", "onDestroy");
        super.onDestroy();
    }

}
