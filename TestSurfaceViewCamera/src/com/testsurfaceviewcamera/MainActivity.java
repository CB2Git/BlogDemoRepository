
package com.testsurfaceviewcamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    /**
     * 拍照的按钮
     */
    private Button mTakePicBtn;

    /**
     * 切换摄像头的按钮
     */
    private Button mRotateBtn;

    private CameraSurfaceView mCameraSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTakePicBtn = (Button) findViewById(R.id.main_take_picture_btn);
        mRotateBtn = (Button) findViewById(R.id.main_rotate_btn);
        mTakePicBtn.setOnClickListener(this);
        mRotateBtn.setOnClickListener(this);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.main_camera_view);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_take_picture_btn) {
            // 拍照
            mCameraSurfaceView.takePicture();
        }

        if (v.getId() == R.id.main_rotate_btn) {
            // 切换摄像头
            mCameraSurfaceView.rotate();
        }
    }

    // 在resume的时候打开摄像头
    @Override
    protected void onResume() {
        mCameraSurfaceView.startCamera();
        super.onResume();
    }

    // 在onPause的时候关闭摄像头
    @Override
    protected void onPause() {
        mCameraSurfaceView.releaseCamera();
        super.onPause();
    }
}
