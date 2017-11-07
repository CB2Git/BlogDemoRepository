
package org.demo.viewdraghelper.widget;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 参考链接{@link http://www.cnblogs.com/punkisnotdead/p/4724825.html}
 */
public class ViewDragHelperDemo extends LinearLayout {

    private static final String TAG = ViewDragHelperDemo.class.getSimpleName();

    private ViewDragHelper mViewDragHelper;

    private ViewDragHelper.Callback mDragCallback;

    public ViewDragHelperDemo(Context context) {
        this(context, null);
    }

    public ViewDragHelperDemo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewDragHelperDemo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        initDragCallBack();
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mDragCallback);
        // 设置为可以捕获屏幕左边的滑动
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 锁屏后再点亮屏幕会调用一下onLayout，不加判断会让布局还原(为什么???)
        if (changed) {
            super.onLayout(changed, l, t, r, b);
        }
    }

    private void initDragCallBack() {
        mDragCallback = new ViewDragHelper.Callback() {

            /**
             * 返回true，表示传入的View可以被拖动
             */
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                // 只允许第一个被拖动
                return child.equals(getChildAt(0));
            }

            /**
             * 传入View即将到达的位置(left)，返回值为真正到达的位置
             */
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                LinearLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int leftBorder = getPaddingLeft() + lp.leftMargin;
                int rightBorder = getMeasuredWidth() - getPaddingRight() - child.getMeasuredWidth() - lp.rightMargin;
                return Math.min(Math.max(leftBorder, left), rightBorder);
            }

            /**
             * 传入View即将到达的位置(top)，返回值为真正到达的位置
             */
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }

            /**
             * 返回横向能拖动的长度，默认返回0，如果被拖动的View设置了点击事件，返回0会不响应点击事件
             */
            @Override
            public int getViewHorizontalDragRange(View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            /**
             * 返回纵向能拖动的长度，默认返回0，如果被拖动的View设置了点击事件，返回0会不响应点击事件
             */
            @Override
            public int getViewVerticalDragRange(View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                Log.i(TAG, "onEdgeDragStarted;" + edgeFlags);
                // 当从屏幕左边开始滑动的时候，开始滑动第一个子控件
                mViewDragHelper.captureChildView(getChildAt(0), pointerId);
            }

            /**
             * 当手指离开以后的回调
             * 
             * @param releasedChild 子View
             * @param xvel X轴的速度
             * @param yvel Y轴的速度
             */
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if (releasedChild.equals(getChildAt(0))) {
                    Log.i(TAG, "xvel = " + xvel + ",yvel=" + yvel);
                    // 手指松开以后自动回到原始位置
                    mViewDragHelper.settleCapturedViewAt(100, 100);
                    invalidate();
                }
            }

            /**
             * 当某一个View在动的时候的回调，不管是用户手动滑动，还是使用settleCapturedViewAt或者smoothSlideViewTo，都会回调这里
             */
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                Log.i(TAG, "left=" + left + ",top=" + top + ",dx=" + dx + ",dy=" + dy);
            }
        };
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }
}
