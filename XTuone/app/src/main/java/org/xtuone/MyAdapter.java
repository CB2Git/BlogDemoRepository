
package org.xtuone;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MyAdapter extends PagerAdapter implements View.OnClickListener {

    private String[] mCourses = new String[] {
            "计算机网络@J2-103", "大学英语@S2-101",
            "高数@S6-112", "大学语文@J4-203",
            "数据结构@J1-409", "离散数学@J3-302",
            "论文指导@J2-100"
    };

    private Context mContext;

    private int mColors[] = new int[] {
            0xFF5A8987, 0xFF678F8D, 0xFF78A98C
    };

    private View[] mViews;

    public MyAdapter(Context context) {
        mContext = context;
        mViews = new View[mCourses.length];
    }

    public MyAdapter(Context context, String[] date) {
        mContext = context;
        mCourses = date;
        mViews = new View[mCourses.length];
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mViews[position] == null) {
            TextView tv = new TextView(container.getContext());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            tv.setGravity(Gravity.CENTER);
            tv.setText(mCourses[position]);
            tv.setBackgroundColor(mColors[position % mColors.length]);
            tv.setOnClickListener(this);
            mViews[position] = tv;
        }
        container.addView(mViews[position]);
        return mViews[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViews[position]);
    }

    @Override
    public int getCount() {
        return mCourses.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            Toast.makeText(mContext, ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
        }
    }

    public int indexView(View v) {
        int index = -1;
        for (int i = 0; i < mViews.length; i++) {
            if (mViews[i] != null && mViews[i].equals(v)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
