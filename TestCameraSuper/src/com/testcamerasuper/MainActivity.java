
package com.testcamerasuper;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private CameraSurfaceView mCameraSurfaceView;

    private CustomSurfaceView mCustomSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera_surfaceview);
        mCustomSurfaceView = (CustomSurfaceView) findViewById(R.id.custom_surface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraSurfaceView.startCamera();
        mCameraSurfaceView.setCustomSurfaceView(mCustomSurfaceView);
    }

    @Override
    protected void onPause() {
        mCameraSurfaceView.releaseCamera();
        super.onPause();
    }
}
