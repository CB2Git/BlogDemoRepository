
package com.testsurfaceviewcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * 显示相机预览界面的SurfaceView
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraSurfaceView";

    private SurfaceHolder mSurfaceHolder;

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
                outputDeviceInfo(param);
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
     * 拍照
     */
    public void takePicture() {
        Log.i(TAG, "takePicture");
        if (mCamera != null) {
            // 关闭快门声音
            mCamera.enableShutterSound(false);
            // 这里由于本人测试的手机前置摄像头无法自动对焦，所以分开处理
            if (FACING_MODE == CameraInfo.CAMERA_FACING_BACK) {
                // 聚焦成功以后才拍照
                mCamera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.i(TAG, "AutoFocus success = " + success);
                        if (success) {
                            reallyTakePicture();
                            mCamera.cancelAutoFocus();
                        }
                    }
                });
            }
            if (FACING_MODE == CameraInfo.CAMERA_FACING_FRONT) {
                reallyTakePicture();
            }
        }
    }

    /**
     * 真正开始拍照
     */
    protected void reallyTakePicture() {
        mCamera.takePicture(new ShutterCallback() {

            // 拍照之前的工作
            @Override
            public void onShutter() {
                Log.i(TAG, "before takePicture");
            }
        }, new PictureCallback() {

            // 照片的二进制数据
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "doing onPictureTaken");
            }
        }, new PictureCallback() {

            // 最终的照片数据
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "done onPictureTaken");
                outOriginPicture(data);
                outWatermarkPicture(data);
                Toast.makeText(getContext(), "保存原始图片和水印图片成功", Toast.LENGTH_SHORT).show();
                // 拍照以后会停止预览，所以继续预览
                mCamera.startPreview();
            }
        });

    }

    /**
     * 保存添加了水印的图片
     * 
     * @param data 图片的元素数据
     */
    private void outWatermarkPicture(byte[] data) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File picPath = new File(externalStorageDirectory, "img_watermake.jpg");
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setTextSize(150);
        canvas.drawText("水印文字", 0, 150, paint);
        try {
            FileOutputStream out = new FileOutputStream(picPath);
            createBitmap.compress(CompressFormat.JPEG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存原始图片
     * 
     * @param data 原始图片的二进制数据
     */
    private void outOriginPicture(byte[] data) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File picPath = new File(externalStorageDirectory, "img.jpg");
        try {
            FileOutputStream out = new FileOutputStream(picPath);
            out.write(data);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * 打印设备信息
     */
    private void outputDeviceInfo(Camera.Parameters param) {
        // 查询屏幕的宽和高
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        Log.i(TAG, "Screen Size：" + metrics.widthPixels + " 屏幕高度:" + metrics.heightPixels);
        Log.i(TAG, "-----------------------------------------------------");
        // 查询camera支持的picturesize和previewsize
        List<Size> pictureSizes = param.getSupportedPictureSizes();
        List<Size> previewSizes = param.getSupportedPreviewSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Size size = pictureSizes.get(i);
            Log.i(TAG, "initCamera:Support PictureSizes: width = " + size.width + "height = " + size.height);
        }
        Log.i(TAG, "-----------------------------------------------------");
        for (int i = 0; i < previewSizes.size(); i++) {
            Size size = previewSizes.get(i);
            Log.i(TAG, "initCamera:Support Sizes: width = " + size.width + "height = " + size.height);
        }
        Log.i(TAG, "-----------------------------------------------------");
        // 查询摄像头信息
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.i(TAG, "numberOfCameras = " + numberOfCameras);
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            // 前置摄像头 = 1
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                Log.i(TAG, "cameraId = " + i + ",Camera is CameraInfo.CAMERA_FACING_FRONT," + "Camera orientation = "
                        + cameraInfo.orientation);
            }
            // 后置摄像头 = 0
            else if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                Log.i(TAG, "cameraId = " + i + ",Camera is CameraInfo.CAMERA_FACING_FRONT," + "Camera orientation = "
                        + cameraInfo.orientation);
            } else {
                Log.e(TAG, "unkonw Camera");
            }
        }
    }
}
