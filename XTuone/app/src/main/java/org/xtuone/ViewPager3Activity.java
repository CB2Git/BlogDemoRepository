
package org.xtuone;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ViewPager3Activity extends AppCompatActivity implements ViewPager.PageTransformer {

    private ViewPager mViewPager;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager3);
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        //(*)为首尾添加的数据
        mAdapter = new MyAdapter(this, new String[]{
                "论文指导@J2-100(*)", "离散数学@J3-302(*)",
                "计算机网络@J2-103", "大学英语@S2-101",
                "高数@S6-112", "大学语文@J4-203",
                "数据结构@J1-409", "离散数学@J3-302",
                "论文指导@J2-100",
                "计算机网络@J2-103(*)", "大学英语@S2-101(*)"
        });
        mViewPager.setAdapter(mAdapter);
        int itemWidth = (getResources().getDisplayMetrics().widthPixels) / 3;
        LinearLayout.LayoutParams layoutParams = (LayoutParams) mViewPager
                .getLayoutParams();
        layoutParams.leftMargin = itemWidth / 2;
        layoutParams.rightMargin = itemWidth / 2;
        mViewPager.setLayoutParams(layoutParams);
        mViewPager.setPageMargin(40);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageTransformer(true, this);
        // 设置当前的为真正的"第一个"
        mViewPager.setCurrentItem(2);
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
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state != ViewPager.SCROLL_STATE_IDLE) {
                    return;
                }
                int item = mViewPager.getCurrentItem();
                // 当前在最后一个
                if (item == mViewPager.getAdapter().getCount() - 2) {
                    mViewPager.setCurrentItem(2, false);
                }
                //当前在第一个
                if (item == 1) {
                    mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 4, false);
                }
            }
        });
        ViewPagerScroller scroller = new ViewPagerScroller(this);
        scroller.setScrollDuration(2000);
        scroller.initViewPagerScroll(mViewPager);
    }

    public void onClick(View v) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //这里不需要担心越界问题，因为到了最后几个会自动回到前面，造成无限循环的假象
                            //TODO 小米机型上面测试的时候，activity不可见以后，再回来动画停止了
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        }
                    });
                    //这个时间不能太短，不然可能上面切换位置还没走完
                    SystemClock.sleep(3000);
                }
            }
        }).start();
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
