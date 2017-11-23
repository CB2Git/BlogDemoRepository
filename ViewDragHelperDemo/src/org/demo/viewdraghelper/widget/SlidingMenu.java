
package org.demo.viewdraghelper.widget;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class SlidingMenu extends LinearLayout {

    private ViewDragHelper mViewDragHelper;

    private ViewDragCallBackImpl mViewDragCallBackImpl = new ViewDragCallBackImpl();

    // 最大open的比例
    private float max_open = 0.8f;

    public SlidingMenu(Context context) {
        super(context);
        mViewDragHelper = ViewDragHelper.create(this, mViewDragCallBackImpl);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewDragHelper = ViewDragHelper.create(this, mViewDragCallBackImpl);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mViewDragHelper = ViewDragHelper.create(this, mViewDragCallBackImpl);
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        View v = findViewWithTag("content");
        // 内容是覆盖菜单的
        if (v != null) {
            v.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }
    }

    private class ViewDragCallBackImpl extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return isTargetView(child);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // 只能滚动到最左边，而且有右边界
            int leftBorder = 0;
            int rightBorder = (int) (getMeasuredWidth() * max_open);
            return Math.min(Math.max(leftBorder, left), rightBorder);
        }

        
        
        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mViewDragHelper.captureChildView(findViewWithTag("content"), pointerId);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (isTargetView(releasedChild)) {
                int left = releasedChild.getLeft();
                if (left < getMeasuredWidth() * max_open / 2) {
                    mViewDragHelper.settleCapturedViewAt(0, 0);
                } else {
                    mViewDragHelper.settleCapturedViewAt((int) (getMeasuredWidth() * max_open), 0);
                }
                postInvalidate();
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            // 垂直方向不准滚动
            return 0;
        }

        private boolean isTargetView(View v) {
            return v != null && "content".equals(v.getTag());
        }

    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

}
