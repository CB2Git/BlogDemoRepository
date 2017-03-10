
package com.testcamerarecorder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * 显示相机预览界面的SurfaceView
 */
@SuppressWarnings("deprecation")
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraSurfaceView";

    private SurfaceHolder mSurfaceHolder;

    private Camera mCamera;

    private boolean mIsCreated = false;

    /** 判断是前置摄像头还是后置摄像头 */
    private int FACING_MODE = CameraInfo.CAMERA_FACING_BACK;

    private MediaRecorder mMediaRecorder;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mIsCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        releaseCamera();
        startCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        stopRecorder();
        releaseCamera();
        mIsCreated = false;
    }

    /**
     * 打开摄像头并开始预览
     */
    public void startCamera() {
        if (!mIsCreated) {
            Log.e(TAG, "surfaceview not create!!!");
            return;
        }
        // 如果没有摄像头
        if (Camera.getNumberOfCameras() == 0) {
            Toast.makeText(getContext(), "没有发现摄像头设备", Toast.LENGTH_SHORT).show();
            return;
        }
        // 只有一个摄像头，那么就不切换摄像头了，
        if (Camera.getNumberOfCameras() == 1) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);
            FACING_MODE = cameraInfo.facing;
        }
        // open() 无参数 默认后置摄像头,没有后置则返回null
        // open(int cameraId) 可选开启摄像头
        // 后置摄像头:CameraInfo.CAMERA_FACING_BACK = 0
        // 前置摄像头:CameraInfo.CAMERA_FACING_FRONT = 1
        mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                Camera.Parameters param = mCamera.getParameters();
                param.setPictureFormat(ImageFormat.JPEG);
                // 设置大小和方向等参数
                Size bestPerviewSize = getBestSupportedSize(param.getSupportedPreviewSizes(), getWidth(), getHeight());
                param.setPreviewSize(bestPerviewSize.width, bestPerviewSize.height);
                Size bestPictureSize = getBestSupportedSize(param.getSupportedPictureSizes(), getWidth(), getHeight());
                param.setPictureSize(bestPictureSize.width, bestPictureSize.height);
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                // 后置拍照的结果需要旋转90度
                if (FACING_MODE == CameraInfo.CAMERA_FACING_BACK) {
                    param.setRotation(90);
                }
                // 前置拍照的结果需要旋转270度
                if (FACING_MODE == CameraInfo.CAMERA_FACING_FRONT) {
                    param.setRotation(270);
                }
                // 预览需要旋转90度
                mCamera.setDisplayOrientation(90);
                mCamera.setParameters(param);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "打开摄像异常", Toast.LENGTH_SHORT).show();
                releaseCamera();
            }
        }
    }

    public void startRecorder() {
        File outputPath = new File(Environment.getExternalStorageDirectory(), "recoder.mp4");
        if (mMediaRecorder == null) {
            // Initial 状态
            mMediaRecorder = new MediaRecorder();
        }
        if (mCamera != null) {
            mCamera.unlock();
        }
        mMediaRecorder.setCamera(mCamera);
        // 前置摄像头需要旋转90度，原因见前几篇博客
        mMediaRecorder.setOrientationHint(90);
        // 进入Initialized状态
        // 设置音频来源为麦克风
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置视频来源为摄像头
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // 进入DataSourceConfigured状态
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // 设置文件保存路径
        mMediaRecorder.setOutputFile(outputPath.getAbsolutePath());
        mMediaRecorder.setVideoSize(1280, 720);
        // 设置帧数率 30fps
        mMediaRecorder.setVideoFrameRate(30);
        // 设置bit率
        mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        try {
            // 进入prepare状态
            mMediaRecorder.prepare();
            // 进入Recording状态
            mMediaRecorder.start();
            Toast.makeText(getContext(), "开始录像", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            Toast.makeText(getContext(), "停止录像", Toast.LENGTH_SHORT).show();
            if (mCamera != null) {
                try {
                    mCamera.reconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 销毁摄像头
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 通过对比得到与宽高比最接近的尺寸（如果有相同尺寸，优先选择）
     * 
     * @param supportedSizeList 需要对比的预览尺寸列表
     * @param surfaceWidth 需要被进行对比的原宽
     * @param surfaceHeight 需要被进行对比的原高
     * @return 得到与原宽高比例最接近的尺寸
     */
    protected Camera.Size getBestSupportedSize(List<Size> supportedSizeList, int surfaceWidth, int surfaceHeight) {
        Log.i(TAG, "surfaceWidth = " + surfaceWidth + ",surfaceHeight = " + surfaceHeight);
        int reqWidth = surfaceWidth;
        int reqHeight = surfaceHeight;
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isPortrait) {
            reqWidth = surfaceHeight;
            reqHeight = surfaceWidth;
        }
        // 先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : supportedSizeList) {
            if ((size.width == reqWidth) && (size.height == reqHeight)) {
                return size;
            }
        }

        // 如果没有尺寸相同的，则找与传入的宽高比最接近的size
        float reqRatio = ((float) reqWidth) / reqHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : supportedSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        Log.i(TAG, "retSize.width = " + retSize.width + ",retSize.height = " + retSize.height);
        return retSize;
    }

}
