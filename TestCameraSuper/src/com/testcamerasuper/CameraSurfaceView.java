
package com.testcamerasuper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * 相机预览窗口
 */
@SuppressWarnings("deprecation")
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

    private static final String TAG = "CameraSurfaceView";

    private SurfaceHolder mSurfaceHolder;

    private SurfaceView mCustomSurfaceView;

    private Camera mCamera;

    private boolean mIsCreated = false;

    /** 判断是前置摄像头还是后置摄像头 */
    private int FACING_MODE = CameraInfo.CAMERA_FACING_BACK;

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
        mCamera = Camera.open(FACING_MODE);
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
                mCamera.setPreviewCallback(this);
                // 设置预览窗体大小为窗口大小
                mSurfaceHolder.setFixedSize(bestPerviewSize.width, bestPerviewSize.height);
                // 加了水印的预览窗口也需要改变大小
                if (mCustomSurfaceView != null) {
                    mCustomSurfaceView.getHolder().setFixedSize(bestPerviewSize.width, bestPerviewSize.height);
                }
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "打开摄像异常", Toast.LENGTH_SHORT).show();
                releaseCamera();
            }
        }
    }

    /**
     * 销毁摄像头
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 切换摄像头
     */
    public void rotate() {
        if (Camera.getNumberOfCameras() <= 1) {
            Toast.makeText(getContext(), "只有一个设备可用", Toast.LENGTH_SHORT).show();
        } else {
            releaseCamera();
            FACING_MODE = 1 - FACING_MODE;
            startCamera();
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

    public void setCustomSurfaceView(SurfaceView surfaceView) {
        mCustomSurfaceView = surfaceView;
    }

    private Paint paint;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize();
        if (mCustomSurfaceView == null) {
            return;
        }
        // 将NV21类型的数据转为Image
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
        if (yuvImage != null) {
            initPaint();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, out);
            // 得到原始图片
            Bitmap bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
            SurfaceHolder customSurfaceHolder = mCustomSurfaceView.getHolder();
            // 获取到画布
            Canvas canvas = customSurfaceHolder.lockCanvas();
            // 将加了水印的图片绘制到预览窗口
            canvas.drawBitmap(BitmapUtil.rotateBitmapAndWaterMark(bitmap, 90, "www.27house.cn", paint), 0, 0, null);
            customSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void initPaint() {
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(50);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(Color.BLUE);
        }
    }
}
