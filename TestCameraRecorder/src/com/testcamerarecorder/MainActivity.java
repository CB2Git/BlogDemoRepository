
package com.testcamerarecorder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    private Button mBeginRecorder;

    private CameraSurfaceView mCameraSurfaceView;

    private boolean mIsRecoreder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBeginRecorder = (Button) findViewById(R.id.recorder);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceview);
        mBeginRecorder.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        mCameraSurfaceView.startCamera();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mIsRecoreder) {
            mCameraSurfaceView.stopRecorder();
            mIsRecoreder = false;
        }
        mCameraSurfaceView.releaseCamera();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.recorder) {
            if (mIsRecoreder) {
                mBeginRecorder.setText("开始录像");
                mCameraSurfaceView.stopRecorder();
            } else {
                mBeginRecorder.setText("停止录像");
                mCameraSurfaceView.startRecorder();
            }
            mIsRecoreder = !mIsRecoreder;
        }
    }

}
