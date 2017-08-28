
package org.xtuone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void method1(View view) {
        Intent intent = new Intent(this, ViewPager1Activity.class);
        startActivity(intent);
    }

    public void method2(View view) {
        Intent intent = new Intent(this, ViewPager2Activity.class);
        startActivity(intent);
    }

    public void method3(View view) {
        Intent intent = new Intent(this, ViewPager3Activity.class);
        startActivity(intent);
    }
}
