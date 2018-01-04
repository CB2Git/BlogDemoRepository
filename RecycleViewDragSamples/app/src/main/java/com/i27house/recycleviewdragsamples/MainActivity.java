package com.i27house.recycleviewdragsamples;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private MyAdapter mAdapter = new MyAdapter();

    private ItemTouchHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.main_recycle_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        helper = new ItemTouchHelper(new ItemTouchHelperCallbackImpl(mAdapter));
        helper.attachToRecyclerView(mRecyclerView);
    }


    private class ItemTouchHelperCallbackImpl extends ItemTouchHelper.Callback {

        private OnItemPositionListener mItemPositionListener;

        public ItemTouchHelperCallbackImpl(OnItemPositionListener itemPositionListener) {
            mItemPositionListener = itemPositionListener;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlag;
            int swipeFlags;
            //如果是表格布局，则可以上下左右的拖动，但是不能滑动
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFlag = ItemTouchHelper.UP |
                        ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT |
                        ItemTouchHelper.RIGHT;
                swipeFlags = 0;
            }
            //如果是线性布局，那么只能上下拖动，只能左右滑动
            else {
                dragFlag = ItemTouchHelper.UP |
                        ItemTouchHelper.DOWN;
                swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            }
            //通过makeMovementFlags生成最终结果
            return makeMovementFlags(dragFlag, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //被拖动的item位置
            int fromPosition = viewHolder.getLayoutPosition();
            //他的目标位置
            int targetPosition = target.getLayoutPosition();
            //为了降低耦合，使用接口让Adapter去实现交换功能
            mItemPositionListener.onItemSwap(fromPosition, targetPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //为了降低耦合，使用接口让Adapter去实现交换功能
            mItemPositionListener.onItemMoved(viewHolder.getLayoutPosition());
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            //当开始拖拽的时候
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
            }
        }

        //当手指松开的时候
        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            super.clearView(recyclerView, viewHolder);
        }

        //禁止长按滚动交换，需要滚动的时候使用{@link ItemTouchHelper#startDrag(ViewHolder)}
        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    }


    public interface OnItemPositionListener {
        //交换
        void onItemSwap(int from, int target);

        //滑动
        void onItemMoved(int position);
    }


    /**
     * 数据默认写死的
     */
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> implements OnItemPositionListener {

        private List<String> mDatas = new ArrayList<>();

        public MyAdapter() {
            for (int i = 0; i < 30; i++) {
                mDatas.add("item:" + i);
            }
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle, parent, false));
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.tv.setText(mDatas.get(position));
        }

        @Override
        public void onItemSwap(int from, int target) {
            Collections.swap(mDatas, from, target);
            notifyItemMoved(from, target);
        }

        @Override
        public void onItemMoved(int position) {
            mDatas.remove(position);
            notifyItemRemoved(position);
        }

        protected class MyHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

            public TextView tv;

            public ImageView drag;

            public MyHolder(View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.item_tv);
                drag = itemView.findViewById(R.id.item_drag);
                drag.setOnTouchListener(this);
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    helper.startDrag(this);
                }
                return false;
            }
        }
    }
}
