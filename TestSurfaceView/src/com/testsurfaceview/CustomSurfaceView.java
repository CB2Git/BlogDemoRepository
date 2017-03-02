
package com.testsurfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 使用SurfaceView实现变色效果
 * 
 * @author Alias {@link #www.27house.cn}
 */
public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CustomSurfaceView";

    private SurfaceHolder mSurfaceHolder = null;

    private DrawThread mDrawThread = null;

    public CustomSurfaceView(Context context) {
        this(context, null);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);
    }

    /**
     * 这个回调在surface第一次被创建以后会立即调用，可以在这个方法里面启动渲染线程，注意只有一个线程可以绘制Surface
     * <p>
     * 所以如果你的绘制操作主要在另一个线程，那么你不应该在这里进行绘图操作
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mDrawThread = new DrawThread(holder);
        mDrawThread.start();
        // 修改SurfaceView大小，会让surfaceChanged会被调用
        // mSurfaceHolder.setFixedSize(500, 500);
    }

    /**
     * 当SurfaceView的尺寸发成变化的时候，这个方法会被立即调用
     * <p>
     * 在{@link #surfaceCreated}方法被调用以后，此方法至少会被调用一次
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
    }

    /**
     * SurfaceView被销毁的时候调用
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mDrawThread.stopDrawThread();
    }

    /**
     * 后台绘制线程
     */
    private static class DrawThread extends Thread {
        private SurfaceHolder mSurfaceHolder = null;

        private boolean mIsAlive = true;

        public DrawThread(SurfaceHolder holder) {
            mSurfaceHolder = holder;
        }

        @Override
        public void run() {
            while (mIsAlive) {
                if (beginTime == 0) {
                    beginTime = System.currentTimeMillis();
                }
                nowTime = System.currentTimeMillis();
                Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas == null) {
                    continue;
                }
                drawBackgroundColor(canvas);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mIsAlive = false;
                    Log.i(TAG, "InterruptedException");
                }
            }
        }

        public void stopDrawThread() {
            mIsAlive = false;
        }

        private long during = 3000;

        private long beginTime = 0;

        private long nowTime = 0;

        private void drawBackgroundColor(Canvas canvas) {
            float fraction = (float) ((nowTime - beginTime + 0.0) / during);
            if (fraction > 1.0) {
                fraction = 0;
                beginTime = nowTime;
            }
            canvas.drawColor(evaluateColor(fraction, 0xff00A2E8, 0xffA349A4));
        }

        /**
         * 计算当前的颜色
         */
        public int evaluateColor(float fraction, int startValue, int endValue) {
            int startInt = startValue;
            int startA = (startInt >> 24);
            int startR = (startInt >> 16) & 0xff;
            int startG = (startInt >> 8) & 0xff;
            int startB = startInt & 0xff;

            int endInt = endValue;
            int endA = (endInt >> 24);
            int endR = (endInt >> 16) & 0xff;
            int endG = (endInt >> 8) & 0xff;
            int endB = endInt & 0xff;

            return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                    (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                    (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                    (int) ((startB + (int) (fraction * (endB - startB))));
        }
    }
}
