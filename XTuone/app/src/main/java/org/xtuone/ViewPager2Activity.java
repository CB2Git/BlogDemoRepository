
package org.xtuone;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ViewPager2Activity extends AppCompatActivity implements ViewPager.PageTransformer {

    private ViewPager mViewPager;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager1);
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mAdapter = new MyAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setClipToPadding(false);
        int itemWidth = (getResources().getDisplayMetrics().widthPixels) / 3;
        // 让左右都留出一个item大小的边距
        mViewPager.setPadding(itemWidth, 0, itemWidth, 0);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageTransformer(true, this);
        // 下面这句可以让默认左右都有数据(item >=3)
        // mViewPager.setCurrentItem(1);
    }

    public void transformPage(View view, float position) {
        if (position < 1 && position >= -1) {
            view.setPivotX(0);
            view.setRotationY(35 * (1 - position));
            // Y轴上面的缩放
            // view.setScaleY((float) (0.1 * position + 0.9));
        }

        if (position > 1 && position <= 3) {
            view.setPivotX(view.getWidth());
            view.setRotationY(-35 * (position - 1));
            // view.setScaleY((float) (-0.1 * position + 1.1));
        }
        if (Math.abs(position - 1) < 0.1) {
            view.setRotationY(0);
            // view.setScaleY(1);
        }
    }
}
