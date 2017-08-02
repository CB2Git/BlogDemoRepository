package org.ndk.ffmpeg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
//        StringBuffer sb = new StringBuffer();
//        sb.append(avformatinfo());
//        sb.append(urlprotocolinfo());
//        sb.append(avformatinfo());
//        sb.append(avfilterinfo());
        //      tv.setText(sb.toString());
        tv.setText(stringFromJNI());
    }


    public native String stringFromJNI();

    public native String avformatinfo();

    public native String urlprotocolinfo();

    public native String avcodecinfo();

    public native String avfilterinfo();
}
