package com.fx.device;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/30.
 * area in home
 */

public class DropMenuPop extends PopupWindow {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private List<String> list = new ArrayList<>();
    private MyAdapter mAdapter;

    public interface onInfoItemSelectedListener {
        void onItemClick(String str);
    }

    private onInfoItemSelectedListener mListener;

    public onInfoItemSelectedListener getmListener() {
        return mListener;
    }

    public void setmListener(onInfoItemSelectedListener mListener) {
        this.mListener = mListener;
    }

    public DropMenuPop(Context context) {
        super(context);
        this.mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.drop_menu_pop, null);
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(0X44000000));
        setFocusable(true);
        setOutsideTouchable(true);
        setContentView(view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.pop_content);
        init();
    }

    private void init() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MyAdapter(list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRecyclerView.addItemDecoration(new MyDecoration(mContext, MyDecoration.VERTICAL_LIST, R.drawable.recycle_divider));
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    public void updateList(List<String> list) {
        mAdapter.update(list);
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> list;

        public MyAdapter(List<String> list) {
            this.list = list;
        }

        public void update(List<String> newData) {
            this.list.clear();
            this.list.addAll(newData);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drop_menu_item, null);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.tv.setText(list.get(position));
            holder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(list.get(position));
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.drop_menu_item_tv);
            }
        }
    }

}
