
package org.xtuone;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ViewPager1Activity extends AppCompatActivity implements ViewPager.PageTransformer {

    private ViewPager mViewPager;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager2);
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mAdapter = new MyAdapter(this);
        mViewPager.setAdapter(mAdapter);
        int itemWidth = (getResources().getDisplayMetrics().widthPixels) / 3;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mViewPager
                .getLayoutParams();
        layoutParams.leftMargin = itemWidth / 2;
        layoutParams.rightMargin = itemWidth / 2;
        mViewPager.setLayoutParams(layoutParams);
        mViewPager.setPageMargin(40);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageTransformer(true, this);
        // 下面这句可以让默认左右都有数据(item >=3)
        // mViewPager.setCurrentItem(1);
        ((ViewGroup) mViewPager.getParent()).setOnTouchListener(new OnTouchListener() {

            float x;

            @Override
            public boolean onTouch(View v, MotionEvent ev) {

                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    x = ev.getX();
                }
                //如果是点击事件，那么需要处理下，判断是否点在左右两边
                if (ev.getAction() == MotionEvent.ACTION_UP && Math.abs(ev.getX() - x) < 20) {
                    View view = viewOfClickOnScreen(ev);
                    if (view != null) {
                        // int index = mViewPager.indexOfChild(view);
                        int index = mAdapter.indexView(view);
                        if (index != mViewPager.getCurrentItem()) {
                            mViewPager.setCurrentItem(index);
                            return true;
                        }
                    }
                }
                return mViewPager.dispatchTouchEvent(ev);
            }
        });
    }

    /**
     * 判断当前点击的位置在ViewPager的哪一个View上面
     */
    private View viewOfClickOnScreen(MotionEvent ev) {
        int childCount = mViewPager.getChildCount();
        int[] location = new int[2];
        for (int i = 0; i < childCount; i++) {
            View v = mViewPager.getChildAt(i);
            v.getLocationOnScreen(location);

            int minX = location[0];
            int minY = mViewPager.getTop();

            int maxX = location[0] + v.getWidth();
            int maxY = mViewPager.getBottom();

            float x = ev.getX();
            float y = ev.getY();

            if ((x > minX && x < maxX) && (y > minY && y < maxY)) {
                return v;
            }
        }
        return null;
    }

    public void transformPage(View view, float position) {
        if (position < -1) {
            view.setScaleY(0.8f);
        } else if (position < 0) {
            view.setScaleY(0.2f * position + 1);
        } else if (position < 1) {
            view.setScaleY(-0.2f * position + 1);
        } else {
            view.setScaleY(0.8f);
        }
    }
}
